package msocket.simple.client

import csw.simple.api.client.SimpleClient
import csw.simple.api.{Codecs, PostRequest, WebsocketRequest}
import msocket.impl.{PostRequestClientJs, WebsocketRequestClientJs}
import scala.concurrent.ExecutionContext.Implicits.global

object ClientAppJs extends Codecs {

  def main(args: Array[String]): Unit = {

    println("abc")
    println("abc")

    val websocketClient = new WebsocketRequestClientJs[WebsocketRequest]("ws://localhost:5000/websocket")
    val postClient      = new PostRequestClientJs[PostRequest]("http://localhost:5000/post")
    val simpleClient    = new SimpleClient(websocketClient, postClient)

//    simpleClient.getNumbers(3).mapMaterializedValue(_.onComplete(println)).runForeach(println)
//    simpleClient.getNames(5).runForeach(println)

//    Thread.sleep(2000)
    simpleClient.hello("msuhtaq").onComplete(println)
//    simpleClient.helloStream("mushtaq").runForeach(println)
//    simpleClient.hello("msuhtaq1").onComplete(println)
//    simpleClient.square(3).onComplete(println)
//    simpleClient.square(4).onComplete(println)
  }
}
