# Container in which to build the application
FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim as builder

# Copy the source code into the builder container
WORKDIR /micronaut/src/hello-world
COPY . .

# Build the application in the builder container
RUN ./gradlew assemble

# Container in which to run the application
FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim

# Copy the jar from the builder container into the run container
COPY --from=builder /micronaut/src/hello-world/build/libs/hello-world-*-all.jar hello-world.jar

# Run the application
EXPOSE 8080
CMD java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar hello-world.jar
