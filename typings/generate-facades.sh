#!/usr/bin/env bash

stc -f slinky --scalajs 1.1.0 --scala 2.13.2 -s es6 \
  --publish-to-bintray-repo=tmtyped \
  --publish-git-repo-link=https://github.com/tmtsoftware/msocket \
  --organization org.scalablytyped
