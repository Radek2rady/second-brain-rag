# 1. Fáze: Sestavení aplikace
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
# Spustí build bez testů (v cloudu šetříme čas a kvóty)
RUN ./gradlew bootJar -x test

# 2. Fáze: Samotný běh
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Kopírujeme jen vygenerovaný jar z první fáze
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]