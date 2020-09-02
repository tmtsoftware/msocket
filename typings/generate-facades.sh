#!/usr/bin/env bash

stc -f slinky --scalajs 1.1.1 --scala 2.13.3 --enableScalaJSDefined all \
--stdlib ESNext,DOM,DOM.Iterable,ScriptHost \
  --publish-to-bintray-repo=tmtyped \
  --publish-git-repo-link=https://github.com/tmtsoftware/msocket \
  --organization org.scalablytyped
