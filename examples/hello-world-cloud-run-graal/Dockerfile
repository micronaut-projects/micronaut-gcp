# Stage 1: Build the JAR
FROM gradle:5.3.1 as gradle
COPY --chown=gradle . /home/app
WORKDIR /home/app
RUN gradle assemble --no-daemon

# Stage 2: Build the native image
FROM oracle/graalvm-ce:1.0.0-rc15 as graalvm
COPY --from=gradle /home/app/build/libs/*.jar /home/app/server.jar
WORKDIR /home/app
RUN native-image --no-server -cp server.jar

# Stage 3: Prepare Server
FROM frolvlad/alpine-glibc
EXPOSE 8080
COPY --from=graalvm /home/app/hello-world .
ENTRYPOINT ["./hello-world"]