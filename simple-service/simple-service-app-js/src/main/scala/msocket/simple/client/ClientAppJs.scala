package msocket.simple.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.simple.api.client.SimpleClient
import csw.simple.api.{Codecs, SimpleRequest}
import msocket.impl.post.PostClientJs
import msocket.impl.sse.SseClientJs
import msocket.impl.ws.WebsocketClientJs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{DurationLong, FiniteDuration}

object ClientAppJs extends Codecs {

  def main(args: Array[String]): Unit = {
    implicit val streamingDelay: FiniteDuration = 1.second

    val websocketClient = new WebsocketClientJs[SimpleRequest]("ws://localhost:5000/websocket")
    val sseClient       = new SseClientJs[SimpleRequest]("http://localhost:5000/sse")
    val postClient      = new PostClientJs[SimpleRequest]("http://localhost:5000/post")

    val simpleClient = new SimpleClient(postClient)

//    val numberStream: Source[Int, Future[Option[String]]] = simpleClient.getNumbers(3)
//    numberStream.mat.onComplete(println)
//    numberStream.onMessage = x => println(s"*******$x")

//    val nameStream: Source[String, NotUsed] = simpleClient.getNames(5)
//    nameStream.onMessage = println

    simpleClient.hello("mushtaq").onComplete(println)
    val postHelloStream: Source[String, NotUsed] = simpleClient.helloStream("mushtaq")
    postHelloStream.onMessage = x => println(s"******$x")

    simpleClient.hello("msuhtaq1").onComplete(println)
//    simpleClient.square(3).onComplete(println)
//    simpleClient.square(4).onComplete(println)

  }
}
