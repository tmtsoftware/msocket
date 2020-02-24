![CI](https://github.com/tmtsoftware/msocket/workflows/CI/badge.svg?branch=master) &nbsp;&nbsp;
![CI](https://github.com/tmtsoftware/msocket/workflows/CI/badge.svg?branch=master&event=release)

What is MSocket?
---------------

MSocket is an opinionated library for building services in Scala and consuming them
in Scala or Scala.js
It abstracts over the underlying transport 
and ensures that swapping a transport layer does not require much code change.
Right now it supports both requestResponse and requestStream protocols.
In that sense it is heavily inspired from RSocket protocol which (no surprise) is
available as one of the transports for MSocket. 
It has a dependency on wonderful 'borer' library for providing 
JSON and CBOR serialization of messages.

MSocket provides
1. Multi-platform clients for JVM (Scala) as well as Javascript (Scala.js)
2. Multi-lingual encoding for messages: Text (JSON) and Binary (CBOR)
3. Multi-transport utilities for building and consuming services: Http, WebSocket and RSocket

MSocket modules
---------------

1. msocket-api: platform agnostic APIs that must be implemented 
by each services and their clients
2. msocket-impl: Akka-Http based implementation of helpers utilities 
for service writers, and transport implementations for Http and Websocket
3. msocket-impl-rsocket: RSocket-Java based implementation of helpers utilities 
for service writers, and transport implementation for RSocket
4. msocket-impl-js: Scala.js transport implementation for Http, Websocket and RSocket 

Getting started
---------------

To show the capabilities of MSocket we have created an example implementation 
in `example-service` module with samples that caters to all the transport. 
Specifically, it shows: 
 
1. how to structure the project modules (-api, -impl, -server, -app-jvm and -app-js)
2. what are the dependencies of each of these modules 
(among themselves and on MSocket modules) in the `build.sbt`
3. client-app wiring for each transport-platform-encoding combinations (-app-jvm and -app-js)
4. sample API definitions and mechanical client derivation (-api)
5. sample protocol messages and their borer based codecs (-api) 
6. sample API implementation (-impl)
7. create a Service by defining Handlers and wiring with sample API implementation (-server)

