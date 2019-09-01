package msocket.example.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{Codecs, ExampleRequest}
import msocket.impl.post.PostClientJs
import msocket.impl.sse.SseClientJs
import msocket.impl.ws.WebsocketClientJs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationLong, FiniteDuration}

object ClientAppJs extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val streamingDelay: FiniteDuration = 1.second

    val websocketClient = new WebsocketClientJs[ExampleRequest]("ws://localhost:5000/websocket")
    val sseClient       = new SseClientJs[ExampleRequest]("http://localhost:5000/sse")
    val postClient      = new PostClientJs[ExampleRequest]("http://localhost:5000/post")

    val exampleClient = new ExampleClient(postClient)

//    val numberStream: Source[Int, Future[Option[String]]] = exampleClient.getNumbers(3)
//    numberStream.mat.onComplete(println)
//    numberStream.onMessage = x => println(s"*******$x")

//    val nameStream: Source[String, NotUsed] = exampleClient.getNames(5)
//    nameStream.onMessage = println

    exampleClient.hello("mushtaq").onComplete(println)
    val postHelloStream: Source[String, NotUsed] = exampleClient.helloStream("mushtaq")
    postHelloStream.onMessage = x => println(s"******$x")

    exampleClient.hello("msuhtaq1").onComplete(println)
//    exampleClient.square(3).onComplete(println)
//    exampleClient.square(4).onComplete(println)

  }
}
