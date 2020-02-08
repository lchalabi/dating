FROM openjdk:11.0-jdk-slim
COPY ./build/libs/untitled-1.0-SNAPSHOT.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "untitled-1.0-SNAPSHOT.jar"]
