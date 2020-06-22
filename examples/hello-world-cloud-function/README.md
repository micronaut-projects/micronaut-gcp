# Micronaut Google Cloud Function Example

This example demonstrates writing an application that can be deployed to Google Cloud Function.

## Running Locally

To run the application locally you can use the `runFunction` Gradle task:

```
$ ./gradlew runFunction
```

And then visit http://localhost:8081/pets 

## Deployment

First build the function with:

```bash
$ ./gradlew clean shadowJar
```

Then `cd` into the `build/libs` directory (deployment has to be done from the location where the JAR lives):

```bash
$ cd build/libs
```

To deploy the function make sure you have `gcloud` CLI then run:

```bash
$ gcloud alpha functions deploy myfunction --entry-point io.micronaut.gcp.function.http.HttpFunction --runtime java11 --trigger-http
```

The `myfunction` bit can be changed to whatever you want to name your function.

Choose unauthenticated access if you don't need auth.

To obtain the trigger URL do the following:

```bash
$ YOUR_HTTP_TRIGGER_URL=$(gcloud alpha functions describe myfunction --format='value(httpsTrigger.url)') 
```

You can then use this variable to test the function invocation:

```bash
$ curl -i $YOUR_HTTP_TRIGGER_URL/hello/John
```