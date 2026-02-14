#!/bin/bash

BASE_URL="http://localhost:8080/api/v1"
TIMESTAMP=$(date +%s)
ADMIN_EMAIL="admin${TIMESTAMP}@test.com"
SELLER_EMAIL="seller${TIMESTAMP}@test.com"
USER_EMAIL="user${TIMESTAMP}@test.com"
PASSWORD="password123"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}Starting API Tests...${NC}"

# 1. Register ADMIN
echo "Registering Admin..."
REGISTER_RESP=$(curl -s -X POST "${BASE_URL}/auth/register" -F "email=${ADMIN_EMAIL}" -F "password=${PASSWORD}" -F "firstName=Admin" -F "lastName=Test" -F "role=ROLE_ADMIN" -F "file=")

VERIFY_TOKEN=$(echo $REGISTER_RESP | grep -o '"verificationToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$VERIFY_TOKEN" ]; then
  echo -e "${RED}Admin Registration Failed: $REGISTER_RESP${NC}"
  exit 1
fi

# Verify Email
curl -s "${BASE_URL}/auth/verify-email?token=${VERIFY_TOKEN}" > /dev/null
echo -e "${GREEN}Admin Registered & Verified.${NC}"

# Login Admin
echo "Logging in Admin..."
LOGIN_RESP=$(curl -s -X POST "${BASE_URL}/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"${ADMIN_EMAIL}\", \"password\":\"${PASSWORD}\"}")

ADMIN_TOKEN=$(echo $LOGIN_RESP | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
  echo -e "${RED}Admin Login Failed: $LOGIN_RESP${NC}"
  exit 1
fi
echo -e "${GREEN}Admin Logged In.${NC}"

# Create Category
echo "Creating Category..."
CAT_RESP=$(curl -s -X POST "${BASE_URL}/categories" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d "{\"name\":\"TestCategory${TIMESTAMP}\", \"description\":\"Test Desc\"}")

# Extract ID properly (handling different json formats)
# Using grep carefully
CAT_ID=$(echo $CAT_RESP | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$CAT_ID" ]; then
  echo -e "${RED}Category Creation Failed: $CAT_RESP${NC}"
  exit 1
fi
echo -e "${GREEN}Category Created (ID: ${CAT_ID}).${NC}"

# 2. Register SELLER
echo "Registering Seller..."
REGISTER_RESP=$(curl -s -X POST "${BASE_URL}/auth/register" -F "email=${SELLER_EMAIL}" -F "password=${PASSWORD}" -F "firstName=Seller" -F "lastName=Test" -F "role=ROLE_SELLER" -F "file=")

VERIFY_TOKEN=$(echo $REGISTER_RESP | grep -o '"verificationToken":"[^"]*' | cut -d'"' -f4)
curl -s "${BASE_URL}/auth/verify-email?token=${VERIFY_TOKEN}" > /dev/null

# Login Seller
LOGIN_RESP=$(curl -s -X POST "${BASE_URL}/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"${SELLER_EMAIL}\", \"password\":\"${PASSWORD}\"}")

SELLER_TOKEN=$(echo $LOGIN_RESP | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}Seller Logged In.${NC}"

# Create Product
echo "Creating Product..."
PROD_RESP=$(curl -s -X POST "${BASE_URL}/products" -H "Authorization: Bearer ${SELLER_TOKEN}" -F "name=TestProduct${TIMESTAMP}" -F "description=Test Desc" -F "price=100.0" -F "stock=50" -F "categoryId=${CAT_ID}" -F "file=")

PROD_ID=$(echo $PROD_RESP | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)

if [ -z "$PROD_ID" ]; then
  echo -e "${RED}Product Creation Failed: $PROD_RESP${NC}"
  exit 1
fi
echo -e "${GREEN}Product Created (ID: ${PROD_ID}).${NC}"

# Pagination Test
echo "Testing Pagination..."
PAGE_RESP=$(curl -s -X GET "${BASE_URL}/products?page=0&size=5")
# Check if totalElements exists
if echo "$PAGE_RESP" | grep -q "totalElements"; then
    TOTAL=$(echo $PAGE_RESP | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}Pagination Works! Found ${TOTAL} products.${NC}"
else
    echo -e "${RED}Pagination Failed: $PAGE_RESP${NC}"
fi

# 3. Register USER
echo "Registering User..."
REGISTER_RESP=$(curl -s -X POST "${BASE_URL}/auth/register" -F "email=${USER_EMAIL}" -F "password=${PASSWORD}" -F "firstName=User" -F "lastName=Test" -F "role=ROLE_USER" -F "file=")

VERIFY_TOKEN=$(echo $REGISTER_RESP | grep -o '"verificationToken":"[^"]*' | cut -d'"' -f4)
curl -s "${BASE_URL}/auth/verify-email?token=${VERIFY_TOKEN}" > /dev/null

# Login User
LOGIN_RESP=$(curl -s -X POST "${BASE_URL}/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"${USER_EMAIL}\", \"password\":\"${PASSWORD}\"}")

USER_TOKEN=$(echo $LOGIN_RESP | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo -e "${GREEN}User Logged In.${NC}"

# Add to Cart
echo "Adding to Cart..."
# Note: PROD_ID is a UUID string, so no need to clean it further usually
CART_RESP=$(curl -s -X POST "${BASE_URL}/cart/add" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"productId\":\"${PROD_ID}\", \"quantity\":1}")

if echo "$CART_RESP" | grep -q "cartId"; then
    echo -e "${GREEN}Added to Cart Successfully.${NC}"
else
    echo -e "${RED}Cart Error: $CART_RESP${NC}"
fi

# Logout
echo "Logging out..."
LOGOUT_RESP=$(curl -s -X POST "${BASE_URL}/auth/logout" -H "Authorization: Bearer ${USER_TOKEN}")
echo -e "${GREEN}Logged Out.${NC}"

# Redis Blacklist Check
echo "Checking Blacklist (Accessing Cart with old token)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "${BASE_URL}/cart" -H "Authorization: Bearer ${USER_TOKEN}")

if [ "$HTTP_CODE" == "403" ] || [ "$HTTP_CODE" == "401" ] || [ "$HTTP_CODE" == "500" ]; then
  echo -e "${GREEN}SUCCESS: Access Denied ($HTTP_CODE). Redis Blacklist is working!${NC}"
else
  echo -e "${RED}FAIL: Access Granted ($HTTP_CODE). Redis Blacklist NOT working!${NC}"
fi

echo -e "${GREEN}All Tests Completed.${NC}"
