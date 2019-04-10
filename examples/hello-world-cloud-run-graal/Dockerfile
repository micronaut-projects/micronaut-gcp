FROM oracle/graalvm-ce:1.0.0-rc15 as graalvm
COPY . /home/app/hello-world
WORKDIR /home/app/hello-world
RUN ./gradlew assemble
RUN ls build/libs
RUN native-image --no-server -cp build/libs/hello-world-*.jar

FROM frolvlad/alpine-glibc
EXPOSE 8080
COPY --from=graalvm /home/app/hello-world .
ENTRYPOINT ["./hello-world"]