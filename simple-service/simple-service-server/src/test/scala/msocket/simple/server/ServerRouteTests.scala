package msocket.simple.server

import akka.NotUsed
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.scaladsl.Source
import csw.simple.api.PostRequest.HelloStream
import csw.simple.api.{Codecs, HelloStreamResponse}
import csw.simple.api.StreamRequest.GetNumbers
import mscoket.impl.Encoding.JsonText
import mscoket.impl.HttpCodecs
import org.scalatest.{FunSuite, Matchers}

class ServerRouteTests extends FunSuite with ScalatestRouteTest with Matchers with Codecs with HttpCodecs {
  private val wiring = new Wiring

  test("websocket") {

    val wsClient = WSProbe()

    WS(s"/websocket", wsClient.flow) ~> wiring.simpleServer.routesForTesting ~> check {
      wsClient.sendMessage(JsonText.strictMessage(GetNumbers(3)))
      isWebSocketUpgrade shouldBe true
//      wsClient.expectMessage().asBinaryMessage.getStreamedData.asScala.runForeach(x => println(x.utf8String))
      println(wsClient.expectMessage())
      println(wsClient.expectMessage())
      println(wsClient.expectMessage())
      println(wsClient.expectMessage())
      println(wsClient.expectMessage())

      Thread.sleep(100000)
    }
  }

  test("http-streaming") {
    Post("/post", HelloStream("mushtaq")) ~> wiring.simpleServer.routesForTesting ~> check {
      responseAs[Source[HelloStreamResponse, NotUsed]].runForeach(println)
    }
  }
}
