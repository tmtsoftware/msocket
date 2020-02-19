package msocket.example.server.handlers

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.Source
import csw.example.api.ExampleApi
import csw.example.api.protocol.ExampleCodecs._
import csw.example.api.protocol.ExampleRequest._
import msocket.api.ContentType
import msocket.impl.ws.WebsocketHandler

/**
 * Implements WebsocketHandler for requestStream messages in the protocol
 * These handlers receive message and responds with [[Source]] of [[Message]]
 */
class ExampleWebsocketHandler(exampleApi: ExampleApi, contentType: ContentType)
    extends WebsocketHandler[ExampleRequestStream](contentType) {
  override def handle(message: ExampleRequestStream): Source[Message, NotUsed] = message match {
    case Square(number)          => stream(exampleApi.square(number))
    case HelloStream(name)       => stream(exampleApi.helloStream(name))
    case GetNumbers(divisibleBy) => stream(exampleApi.getNumbers(divisibleBy))
    case RandomBagStream         => stream(exampleApi.randomBagStream())
  }
}
