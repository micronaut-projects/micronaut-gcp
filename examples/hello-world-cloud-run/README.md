# Micronaut + Google Cloud Run

This example demonstrates Micronaut and Google Cloud Run.

## Deploy using [Jib](https://github.com/GoogleContainerTools/jib) and [Google Container Registry](https://cloud.google.com/container-registry/).

### Setup

To get going you should have [Google Cloud SDK installed](https://cloud.google.com/sdk/install) and follow the [Setup Instructions](https://cloud.google.com/run/docs/setup) for Google Cloud Run.

You should then modify the `googleProjectId` reference in `gradle.properties` to your Google Project ID.

### Deployment

To push the container to Container Registry run:

```
$ ./gradlew jib
```

Deployment is then as simple as running:

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

## Deploy using [Cloud Run Button](https://github.com/GoogleCloudPlatform/cloud-run-button)

Simply click the button below to deploy the service.

[![Run on Google Cloud](https://storage.googleapis.com/cloudrun/button.svg)](https://console.cloud.google.com/cloudshell/editor?shellonly=true&cloudshell_image=gcr.io/cloudrun/button&cloudshell_git_repo=https://github.com/micronaut-projects/micronaut-gcp.git&cloudshell_dir=examples/hello-world-cloud-run/)

