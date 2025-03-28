# Etapa 1: Build con Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/mx-bank-wkr-currency-exchange-*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]