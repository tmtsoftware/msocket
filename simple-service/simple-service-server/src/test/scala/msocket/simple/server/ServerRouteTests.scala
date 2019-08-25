package msocket.simple.server

import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import csw.simple.api.Codecs
import csw.simple.api.RequestProtocol.GetNumbers
import mscoket.impl.Encoding.JsonText
import msocket.api.Payload
import org.scalatest.{FunSuite, Matchers}

class ServerRouteTests extends FunSuite with ScalatestRouteTest with Matchers with Codecs {
  private val wiring = new Wiring
  test("demo") {

    val wsClient = WSProbe()
    val encoding = JsonText

    WS(s"/websocket/${encoding.Name}", wsClient.flow) ~> wiring.simpleServer.routesForTesting ~> check {
      wsClient.sendMessage(encoding.strictMessage(Payload(GetNumbers(3))))
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
}
