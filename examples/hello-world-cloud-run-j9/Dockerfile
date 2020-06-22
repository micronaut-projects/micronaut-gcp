# Stage 1: Build the JAR
FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim as gradle
COPY . /home/app
WORKDIR /home/app
RUN ./gradlew test assemble --no-daemon

# Stage 3: Prepare Server
FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
WORKDIR /home/app
COPY --from=gradle /home/app/build/libs/*.jar /home/app/server.jar
COPY --from=gradle /home/app/build/shared-classes /home/app/shared-classes
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAM=256m", "-Xshareclasses:cacheDir=/home/app/shared-classes", "-XX:TieredStopAtLevel=1", "-noverify", "-jar", "server.jar"]