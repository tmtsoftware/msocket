package msocket.simple.server

import akka.NotUsed
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest, WSProbe}
import akka.stream.scaladsl.Source
import csw.simple.api.SimpleRequest.{GetNumbers, HelloStream}
import mscoket.impl.HttpCodecs
import org.scalatest.{FunSuite, Matchers}
import akka.testkit.TestDuration
import csw.simple.api.Codecs
import mscoket.impl.ws.Encoding.JsonText

import scala.concurrent.duration.DurationLong

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
    implicit val timeout: RouteTestTimeout = RouteTestTimeout(10.seconds.dilated)
    Post("/post", HelloStream("mushtaq")) ~> wiring.simpleServer.routesForTesting ~> check {
      responseAs[Source[String, NotUsed]].take(3).runForeach(println)
    }
  }
}
