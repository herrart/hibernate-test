FROM maven:3.8.6-eclipse-temurin-17-alpine as builder
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean compile assembly:single
COPY --from=builder /usr/src/app/target/hibernate-test-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/app/hibernate-test-1.0-SNAPSHOT-jar-with-dependencies.jar

#Copy from local target foler
#FROM eclipse-temurin:17.0.3_7-jre-alpine
#COPY /target/hibernate-test-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/app/hibernate-test-1.0-SNAPSHOT-jar-with-dependencies.jar



ENTRYPOINT ["java","-jar","/usr/app/hibernate-test-1.0-SNAPSHOT-jar-with-dependencies.jar"]