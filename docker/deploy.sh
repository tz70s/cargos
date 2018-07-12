#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

# publish sub projects.
sbt ";project cls; docker:publish"
sbt ";project shelf; docker:publish"
