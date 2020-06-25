# Micronaut GCP

[![Maven Central](https://img.shields.io/maven-central/v/io.micronaut.gcp/micronaut-gcp-common.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.micronaut.gcp%22%20AND%20a:%22micronaut-gcp-common%22)
[![Build Status](https://github.com/micronaut-projects/micronaut-gcp/workflows/Java%20CI/badge.svg)](https://github.com/micronaut-projects/micronaut-gcp/actions)

This project includes integration between [Micronaut](http://micronaut.io) and Google Cloud Platform (GCP).


## Examples

You can generate example GCP Function applications at https://launch.micronaut.io:

* GCP Simple Function - Select Application Type `Serverless Function` and the `gcp-function` feature and generate!
* GCP HTTP Function - Select Application Type `Application` and the `gcp-function` feature and generate!

## Documentation

See the [Documentation](https://micronaut-projects.github.io/micronaut-gcp/latest/guide) for more information. 

See the [Snapshot Documentation](https://micronaut-projects.github.io/micronaut-gcp/snapshot/guide) for the current development docs.

## Snapshots and Releases

Snaphots are automatically published to [JFrog OSS](https://oss.jfrog.org/artifactory/oss-snapshot-local/) using [Github Actions](https://github.com/micronaut-projects/micronaut-gcp/actions).

See the documentation in the [Micronaut Docs](https://docs.micronaut.io/latest/guide/index.html#usingsnapshots) for how to configure your build to use snapshots.

Releases are published to JCenter and Maven Central via [Github Actions](https://github.com/micronaut-projects/micronaut-gcp/actions).

A release is performed with the following steps:

* [Edit the version](https://github.com/micronaut-projects/micronaut-gcp/edit/master/gradle.properties) specified by `projectVersion` in `gradle.properties` to a semantic, unreleased version. Example `1.0.0`
* [Create a new release](https://github.com/micronaut-projects/micronaut-gcp/releases/new). The Git Tag should start with `v`. For example `v1.0.0`.
* [Monitor the Workflow](https://github.com/micronaut-projects/micronaut-gcp/actions?query=workflow%3ARelease) to check it passed successfully.
* Celebrate!
