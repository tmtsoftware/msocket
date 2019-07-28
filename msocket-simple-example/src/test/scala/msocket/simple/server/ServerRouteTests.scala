package msocket.simple.server

import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import csw.simple.api.Codecs
import csw.simple.api.Protocol.GetNumbers
import msocket.core.api.Payload
import org.scalatest.{FunSuite, Matchers}

class ServerRouteTests extends FunSuite with ScalatestRouteTest with Matchers with Codecs {
  private val wiring = new Wiring
  test("demo") {
    val wsClient = WSProbe()
    WS(s"/websocket", wsClient.flow) ~> wiring.simpleServer.routesForTesting ~> check {
      wsClient.sendMessage(wiring.encoding.strict(Payload(GetNumbers(3))))
      isWebSocketUpgrade shouldBe true
      wsClient.expectMessage().asTextMessage.getStreamedText.asScala.runForeach(println)

      Thread.sleep(100000)
    }
  }
}
