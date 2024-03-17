# ----- VANILLA DOCKER FILE --------
# ----------------------------------
# ----------------------------------
FROM maven:3.8.8-eclipse-temurin-17 AS Builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip


FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=Builder /app/target/*.jar /app/application.jar
COPY . .
CMD ["java", "-jar", "application.jar"]



