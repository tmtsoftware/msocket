package msocket.simple.client

import akka.stream.scaladsl.Source
import csw.simple.api.client.SimpleClient
import csw.simple.api.{Codecs, PostRequest, StreamRequest}
import msocket.impl.{PostClientJs, WebsocketClientJs}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ClientAppJs extends Codecs {

  def main(args: Array[String]): Unit = {
    val websocketClient = new WebsocketClientJs[StreamRequest]("ws://localhost:5000/websocket")
    val postClient      = new PostClientJs[PostRequest]("http://localhost:5000/post")
    val simpleClient    = new SimpleClient(postClient, websocketClient)

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
