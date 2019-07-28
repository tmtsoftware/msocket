package msocket.simple.client

import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import csw.simple.api.Protocol.{GetNames, GetNumbers, Hello, Ping, Publish, RequestResponse, RequestStream, Square}
import csw.simple.api.SimpleApi
import msocket.core.api.DoneCodec
import msocket.core.client.ClientSocket

import scala.concurrent.Future

class SimpleClient(socket: ClientSocket[RequestResponse, RequestStream]) extends SimpleApi with DoneCodec {
  override def hello(name: String): Future[String] = socket.requestResponse[String](Hello(name))
  override def square(number: Int): Future[Int]    = socket.requestResponse[Int](Square(number))
  override def ping(msg: String): Future[Done]     = socket.requestResponse[Done](Ping(msg))
  override def publish(number: Int): Future[Done]  = socket.requestResponse[Done](Publish(number))

  override def getNames(size: Int): Source[String, NotUsed]       = socket.requestStream[String](GetNames(size))
  override def getNumbers(divisibleBy: Int): Source[Int, NotUsed] = socket.requestStream[Int](GetNumbers(divisibleBy))

  override def helloAll(names: Source[String, NotUsed]): Source[String, NotUsed] = ???
  override def squareAll(numbers: Source[Int, NotUsed]): Source[Int, NotUsed]    = ???
}
