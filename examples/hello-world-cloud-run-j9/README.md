# Micronaut + Google Cloud Run + OpenJDK J9

This example demonstrates Micronaut and Google Cloud Run using OpenJDK J9 shared classes to optimize cold starts.

## Setup

The example uses a `Dockerfile` that will build the native image.

To get going you should have [Google Cloud SDK installed](https://cloud.google.com/sdk/install) and follow the [Setup Instructions](https://cloud.google.com/run/docs/setup) for Google Cloud Run.

## Deployment

First prepare the image using Google Cloud Build:

```
$ gcloud builds submit --tag gcr.io/[PROJECT-ID]/hello-world 
```

This delegates building of the image to GCP infrastructure. 

You are now ready to deploy your application:

```
$ gcloud beta run deploy --image gcr.io/[PROJECT ID]/hello-world
```

Where `[PROJECT ID]` is replaced for your project ID. You should see output like the following:

```
Service name: (hello-world):  
Deploying container to Cloud Run service [hello-world] in project [PROJECT_ID] region [us-central1]

✓ Deploying... Done.                                                                                                                                                                                                            
  ✓ Creating Revision...                                                                                                                                                                                                        
  ✓ Routing traffic...                                                                                                                                                                                                          
Done.                                                                                                                                                                                                                           
Service [hello-world] revision [hello-world-00004] has been deployed and is serving traffic at https://hello-world-9487r97234-uc.a.run.app
```

The URL is the URL of your Cloud Run application.

# Explanation

The `Dockerfile` is used to run the tests which uses [OpenJ9's Shared Classes](https://www.eclipse.org/openj9/docs/xshareclasses/) feature to pre-compute the shared classes for the application, see `build.gradle`:

```
test.jvmArgs('-Xshareclasses:cacheDir=' + new File(project.buildDir, "shared-classes"))
```

These classes are then copied into the final Docker image and passed to the image in the `Dockerfile`:

```
ENTRYPOINT ["java", "-XX:MaxRAM=256m", "-Xshareclasses:cacheDir=/home/app/shared-classes", "-XX:TieredStopAtLevel=1", "-noverify", "-jar", "server.jar"]
```