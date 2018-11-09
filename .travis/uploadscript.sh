#!/bin/bash

if [[ $TRAVIS_BRANCH == 'master' ]]
  ./gradlew build
  ./gradlew githubRelease
fi
