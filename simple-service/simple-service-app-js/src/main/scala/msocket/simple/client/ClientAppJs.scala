package msocket.simple.client

import akka.stream.scaladsl.Source
import csw.simple.api.client.SimpleClient
import csw.simple.api.{Codecs, PostRequest, StreamRequest}
import msocket.impl.post.PostClientJs
import msocket.impl.sse.SseConnection
import msocket.impl.streaming.StreamingClientJs
import msocket.impl.ws.WebsocketConnection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ClientAppJs extends Codecs {

  def main(args: Array[String]): Unit = {
    val websocketConnection = new WebsocketConnection[StreamRequest]("ws://localhost:5000/websocket")
    val sseConnection       = new SseConnection[StreamRequest]("http://localhost:5000/sse")
    val streamingClient     = new StreamingClientJs[StreamRequest](sseConnection)

    val postClient   = new PostClientJs[PostRequest]("http://localhost:5000/post")
    val simpleClient = new SimpleClient(postClient, streamingClient)

    val numberStream: Source[Int, Future[Option[String]]] = simpleClient.getNumbers(3)
    numberStream.mat.onComplete(println)
    numberStream.onMessage = println

//    val nameStream: Source[String, NotUsed] = simpleClient.getNames(5)
//    nameStream.onMessage = println

    simpleClient.hello("mushtaq").onComplete(println)
//    val postHelloStream: Source[HelloStreamResponse, NotUsed] = simpleClient.helloStream("mushtaq")
//    postHelloStream.onMessage = _

    simpleClient.hello("msuhtaq1").onComplete(println)
    simpleClient.square(3).onComplete(println)
    simpleClient.square(4).onComplete(println)

  }
}
