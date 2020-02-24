package msocket.example.server

import akka.NotUsed
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest, WSProbe}
import akka.stream.scaladsl.Source
import akka.testkit.TestDuration
import csw.example.api.protocol.ExampleRequest.{GetNumbers, Hello, HelloStream}
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import msocket.api.ContentType
import msocket.api.ContentType.Json
import msocket.impl.post.{ClientHttpCodecs, FetchEvent}
import msocket.impl.ws.WebsocketExtensions.WebsocketEncoding
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationLong

class ServerRouteTests extends AnyFunSuite with ScalatestRouteTest with Matchers with ExampleCodecs with ClientHttpCodecs {

  override def clientContentType: ContentType = Json

  private val wiring = new ServerWiring

  test("websocket") {

    val wsClient = WSProbe()

    WS(s"/websocket-endpoint", wsClient.flow) ~> wiring.exampleServer.routesWithCors ~> check {
      wsClient.sendMessage(Json.strictMessage(GetNumbers(3): ExampleRequest))
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
    Post("/post-streaming-endpoint", HelloStream("mushtaq"): ExampleRequest) ~> wiring.exampleServer.routesWithCors ~> check {
      responseAs[Source[FetchEvent, NotUsed]].take(3).runForeach(println)
    }
  }

  test("simple-post") {
    implicit val timeout: RouteTestTimeout = RouteTestTimeout(10.seconds.dilated)
    Post("/post-endpoint", Hello("mushtaq"): ExampleRequest) ~> wiring.exampleServer.routesWithCors ~> check {
      println(status)
      println(response)
    }
  }

}
