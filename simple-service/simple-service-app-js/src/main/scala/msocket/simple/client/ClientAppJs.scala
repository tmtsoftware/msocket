package msocket.simple.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.simple.api.client.SimpleClient
import csw.simple.api.{Codecs, HelloStreamResponse, PostRequest, StreamRequest}
import msocket.impl.post.PostClientJs
import msocket.impl.sse.SseClientJs
import msocket.impl.ws.WebsocketClientJs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationLong, FiniteDuration}

object ClientAppJs extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val timeout: FiniteDuration = 1.second

    val websocketClient = new WebsocketClientJs[StreamRequest]("ws://localhost:5000/websocket")
    val sseClient       = new SseClientJs[StreamRequest]("http://localhost:5000/sse")
    val postClient      = new PostClientJs[PostRequest]("http://localhost:5000/post")

    val simpleClient = new SimpleClient(
      postClient,
      null
    )

//    val numberStream: Source[Int, Future[Option[String]]] = simpleClient.getNumbers(3)
//    numberStream.mat.onComplete(println)
//    numberStream.onMessage = x => println(s"*******$x")

//    val nameStream: Source[String, NotUsed] = simpleClient.getNames(5)
//    nameStream.onMessage = println

    simpleClient.hello("mushtaq").onComplete(println)
    val postHelloStream: Source[HelloStreamResponse, NotUsed] = simpleClient.helloStream("mushtaq")
    postHelloStream.onMessage = x => println(s"******$x")

    simpleClient.hello("msuhtaq1").onComplete(println)
//    simpleClient.square(3).onComplete(println)
//    simpleClient.square(4).onComplete(println)

  }
}
