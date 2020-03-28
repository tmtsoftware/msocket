#!/usr/bin/env bash
if [ "$1" == "production" ]; then
    sbt example-service-app-js/fullOptJS
    cd example-service/example-service-app-js
    npm run build:prod
elif [ "$1" == "development" ]; then
    sbt example-service-app-js/fastOptJS
    cd example-service/example-service-app-js
    npm run start
else
    echo "[ERROR] Please specify which environment to run npm build"
fi


