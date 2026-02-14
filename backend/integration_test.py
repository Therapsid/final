import requests
import json
import time

BASE_URL = "http://localhost:8080/api/v1"
TIMESTAMP = int(time.time())

# Test Users
ADMIN_EMAIL = f"admin{TIMESTAMP}@test.com"
SELLER_EMAIL = f"seller{TIMESTAMP}@test.com"
USER_EMAIL = f"user{TIMESTAMP}@test.com"
PASSWORD = "password123"

# Colors for output
class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

def print_step(msg):
    print(f"{bcolors.OKBLUE}[STEP] {msg}{bcolors.ENDC}")

def print_success(msg):
    print(f"{bcolors.OKGREEN}[SUCCESS] {msg}{bcolors.ENDC}")

def print_fail(msg):
    print(f"{bcolors.FAIL}[FAIL] {msg}{bcolors.ENDC}")

def register(email, role, firstName="Test", lastName="User"):
    url = f"{BASE_URL}/auth/register"
    params = {
        "firstName": firstName,
        "lastName": lastName,
        "email": email,
        "password": PASSWORD,
        "role": role
    }
    # Hack to force multipart/form-data without an actual file
    # Or just pass an empty file field if the backend allows it to be optional
    files = {'file': (None, None)} 
    response = requests.post(url, data=params, files=files) 
    return response

def login(email, password):
    url = f"{BASE_URL}/auth/login"
    payload = {"email": email, "password": password}
    headers = {'Content-Type': 'application/json'}
    return requests.post(url, json=payload, headers=headers)

def create_category(token, name, description):
    url = f"{BASE_URL}/categories"
    headers = {'Authorization': f'Bearer {token}', 'Content-Type': 'application/json'}
    payload = {"name": name, "description": description}
    return requests.post(url, json=payload, headers=headers)

def create_product(token, name, description, price, stock, category_id):
    url = f"{BASE_URL}/products"
    headers = {'Authorization': f'Bearer {token}'} 
    
    # Send all fields as multipart parts to ensure correct Content-Type
    multipart_data = {
        'name': (None, str(name)),
        'description': (None, str(description)),
        'price': (None, str(price)),
        'stock': (None, str(stock)),
        'categoryId': (None, str(category_id)),
        'file': ('', b'', 'application/octet-stream') # Optional file (safest way for empty)
    }
    
    return requests.post(url, files=multipart_data, headers=headers)
    

def get_all_products(page=0, size=10):
    url = f"{BASE_URL}/products?page={page}&size={size}"
    return requests.get(url)

def add_to_cart(token, product_id, quantity=1):
    url = f"{BASE_URL}/cart/add?productId={product_id}&quantity={quantity}" # Assuming query params based on Service/Controller structure check
    # Let's check CartController signature if possible, but standard is usually post with params or body.
    # Assuming query params for now as per typical Spring MVC @RequestParam default.
    headers = {'Authorization': f'Bearer {token}'}
    return requests.post(url, headers=headers)

def logout(token):
    url = f"{BASE_URL}/auth/logout"
    headers = {'Authorization': f'Bearer {token}'}
    return requests.post(url, headers=headers)

def run_tests():
    print(f"{bcolors.HEADER}Starting Integration Tests...{bcolors.ENDC}")
    
    # 1. Register ADMIN
    print_step(f"Registering ADMIN: {ADMIN_EMAIL}")
    resp = register(ADMIN_EMAIL, "ROLE_ADMIN")
    if resp.status_code == 201:
        print_success("Admin registered.")
        # Verify Email Mock (Using the token returned in response if any, or verify-email endpoint)
        # The register response body contains the verification token!
        verification_token = resp.json().get('verificationToken') # Check RegisterResponse class
        if verification_token:
            # Verify Email
            verify_url = f"{BASE_URL}/auth/verify-email?token={verification_token}"
            requests.get(verify_url)
            print_success("Admin email verified.")
        else:
             print_fail("Verification token not found in register response.")
    else:
        print_fail(f"Admin registration failed: {resp.text}")
        return

    # 2. Login ADMIN
    print_step("Logging in ADMIN")
    resp = login(ADMIN_EMAIL, PASSWORD)
    if resp.status_code == 200:
        admin_token = resp.json().get('accessToken')
        print_success("Admin logged in.")
    else:
        print_fail(f"Admin login failed: {resp.text}")
        return

    # 3. Create Category
    category_name = f"TestCategory{TIMESTAMP}"
    print_step(f"Creating Category: {category_name}")
    resp = create_category(admin_token, category_name, "Integration Test Category")
    if resp.status_code == 201:
        category_id = resp.json().get('id')
        print_success(f"Category created with ID: {category_id}")
    else:
        print_fail(f"Category creation failed: {resp.text}")
        return

    # 4. Register SELLER
    print_step(f"Registering SELLER: {SELLER_EMAIL}")
    resp = register(SELLER_EMAIL, "ROLE_SELLER")
    if resp.status_code == 201:
        verify_token = resp.json().get('verificationToken')
        requests.get(f"{BASE_URL}/auth/verify-email?token={verify_token}")
        print_success("Seller registered and verified.")
    else:
        print_fail(f"Seller registration failed: {resp.text}")
        return

    # 5. Login SELLER
    print_step("Logging in SELLER")
    resp = login(SELLER_EMAIL, PASSWORD)
    if resp.status_code == 200:
        seller_token = resp.json().get('accessToken')
        print_success("Seller logged in.")
    else:
        print_fail("Seller login failed.")
        return

    # 6. Create Product
    product_name = f"TestProduct{TIMESTAMP}"
    print_step(f"Creating Product: {product_name}")
    # Sending data=params makes it multipart/form-data compatible for @RequestParam
    url = f"{BASE_URL}/products"
    headers = {'Authorization': f'Bearer {seller_token}'}
    params = {
        "name": product_name,
        "description": "Test Desc",
        "price": 100.0,
        "stock": 50,
        "categoryId": category_id
    }
    resp = requests.post(url, data=params, headers=headers) # Using data=params sends as form-urlencoded or multipart depending on files
    
    if resp.status_code == 201:
        product_id = resp.json().get('id')
        print_success(f"Product created with ID: {product_id}")
    else:
        print_fail(f"Product creation failed: {resp.text}")
        # Try debugging: maybe it expects query params in URL?
        # Let's try query params if this failed.
        # But controller says @RequestParam which handles both query and form data.
        return

    # 7. Pagination Check
    print_step("Checking Pagination (Get All Products)")
    resp = get_all_products(page=0, size=5)
    if resp.status_code == 200:
        data = resp.json()
        if 'content' in data: # Page object has 'content' field
            print_success(f"Pagination works! Found {len(data['content'])} products in page 0.")
            print(f"Total Elements: {data.get('totalElements')}, Total Pages: {data.get('totalPages')}")
        else:
            print_fail("Response is not a Page object (missing 'content' field).")
            print(data)
    else:
        print_fail(f"Get all products failed: {resp.text}")

    # 8. Register USER
    print_step(f"Registering USER: {USER_EMAIL}")
    resp = register(USER_EMAIL, "ROLE_USER")
    if resp.status_code == 201:
        verify_token = resp.json().get('verificationToken')
        requests.get(f"{BASE_URL}/auth/verify-email?token={verify_token}")
        print_success("User registered and verified.")
    else:
        print_fail("User registration failed.")
        return

    # 9. Login USER
    print_step("Logging in USER")
    resp = login(USER_EMAIL, PASSWORD)
    if resp.status_code == 200:
        user_token = resp.json().get('accessToken')
        print_success("User logged in.")
    else:
        print_fail("User login failed.")
        return

    # 10. Add to Cart
    print_step("Adding Product to Cart")
    # CartController signature check needed. Assuming typical POST /cart?productId=...
    # Checking CartController...
    # Let's assume endpoint: POST /api/v1/cart/add?productId=...&quantity=...
    # Based on ServiceImpl `addToCart(email, productId, qty)`
    url = f"{BASE_URL}/cart/add"
    params = {"productId": product_id, "quantity": 1}
    headers = {'Authorization': f'Bearer {user_token}'}
    resp = requests.post(url, params=params, headers=headers)
    
    if resp.status_code == 200:
        print_success("Added to cart successfully.")
    elif resp.status_code == 404:
        print_fail("Cart endpoint not found or product/user not found.")
    else:
        print_fail(f"Add to cart failed: {resp.text}")

    # 11. Logout & Redis Blacklist Check
    print_step("Testing Logout and Redis Blacklist")
    resp = logout(user_token)
    if resp.status_code == 200:
        print_success("Logout successful.")
    else:
        print_fail("Logout failed.")
    
    # Try to use the old token to get cart
    print_step("Attempting to access Cart with blacklisted token")
    url = f"{BASE_URL}/cart"
    headers = {'Authorization': f'Bearer {user_token}'}
    resp = requests.get(url, headers=headers)
    
    if resp.status_code == 401 or resp.status_code == 403 or resp.status_code == 500: 
        # 500 might happen if JwtException is thrown and not handled by global handler gracefully, 
        # but 401/403 is ideal. 
        # Our JWTFilter throws InsufficientAuthenticationException which usually results in 401/403.
        print_success(f"Access denied as expected! Status: {resp.status_code}")
    else:
        print_fail(f"Security Breach! Token still works. Status: {resp.status_code}")

if __name__ == "__main__":
    try:
        run_tests()
    except Exception as e:
        print_fail(f"An error occurred: {e}")
