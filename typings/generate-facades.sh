#!/usr/bin/env bash

stc -f slinky --scalajs 1.2.0 --scala 2.13.3 --enableScalaJSDefined all \
--stdlib ESNext,DOM,DOM.Iterable,ScriptHost \
  --organization org.scalablytyped \
  --publish-to-bintray-repo=tmtyped \
  --publish-git-repo-link=https://github.com/tmtsoftware/msocket
