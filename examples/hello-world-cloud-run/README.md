# Micronaut + Google Cloud Run

This example demonstrates Micronaut and Google Cloud Run.

## Setup

The example uses [Jib](https://github.com/GoogleContainerTools/jib) and [Google Container Registry](https://cloud.google.com/container-registry/).

To get going you should have [Google Cloud SDK installed](https://cloud.google.com/sdk/install) and follow the [Setup Instructions](https://cloud.google.com/run/docs/setup) for Google Cloud Run.

You should then modify the `googleProjectId` reference in `gradle.properties` to your Google Project ID.

## Deployment

Deployment is then as simple as running:

```
$ ./gradlew jib
$ gcloud beta run deploy --image gcr.io/[PROJECT ID]/hello-world
```

Where `[PROJECT ID]` is replaced for your project ID. You should see output like the following:

```
Service name: (hello-world):  
Deploying container to Cloud Run service [hello-world] in project [micronaut-test] region [us-central1]

✓ Deploying... Done.                                                                                                                                                                                                            
  ✓ Creating Revision...                                                                                                                                                                                                        
  ✓ Routing traffic...                                                                                                                                                                                                          
Done.                                                                                                                                                                                                                           
Service [hello-world] revision [hello-world-00004] has been deployed and is serving traffic at https://hello-world-9487r97234-uc.a.run.app
```

The URL is the URL of your Cloud Run application.