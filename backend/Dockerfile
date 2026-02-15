FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Sadece pom.xml'i kopyalayıp bağımlılıkları indiriyoruz.
# Bu katman pom.xml değişmediği sürece cache'den gelir.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kaynak kodları kopyalayıp derleme yapıyoruz.
COPY src ./src
RUN mvn clean package -DskipTests

# Çalıştırma aşaması
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
