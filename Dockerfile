# ----- VANILLA DOCKER FILE --------
# ----------------------------------
# ----------------------------------
FROM maven:3.8.8-eclipse-temurin-17 AS Builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
EXPOSE 8080
# Build application and skip tests initially
RUN mvn clean package -Dmaven.test.skip 

# Recompile and run tests
# RUN mvn compile test-compile && mvn test


 # run with -Dmaven.test.skip if you need to run it without tests


FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=Builder /app/target/*.jar /app/application.jar
COPY . .
CMD ["java", "-jar", "application.jar"]





