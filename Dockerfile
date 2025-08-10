FROM openjdk:23-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ./build/libs/ListMaster-0.0.1-SNAPSHOT.jar app.jar
VOLUME /appdata/
COPY ./build/resources/main/lists/* /appdata/
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 3000
