package msocket.simple.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.simple.api.client.SimpleClient
import csw.simple.api.{Codecs, HelloStreamResponse, PostRequest, StreamRequest}
import msocket.impl.post.{PostClientJs, PostConnectionFactory}
import msocket.impl.sse.SseConnectionFactory
import msocket.impl.streaming.StreamingClientJs
import msocket.impl.ws.WebsocketConnectionFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ClientAppJs extends Codecs {

  def main(args: Array[String]): Unit = {
    val websocketConnectionFactory = new WebsocketConnectionFactory[StreamRequest]("ws://localhost:5000/websocket")
    val sseConnectionFactory       = new SseConnectionFactory[StreamRequest]("http://localhost:5000/sse")
    val postConnectionFactory      = new PostConnectionFactory[PostRequest]("http://localhost:5000/post")

    val streamingClient = new StreamingClientJs[StreamRequest](websocketConnectionFactory)
    val postClient      = new PostClientJs[PostRequest]("http://localhost:5000/post", postConnectionFactory)

    val simpleClient = new SimpleClient(postClient, streamingClient)

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
