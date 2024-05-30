package msocket.example.server

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.adapter._
import org.apache.pekko.http.scaladsl.model.ws.TextMessage
import org.apache.pekko.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest, WSProbe}
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.testkit.TestDuration
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleProtocol._
import msocket.api.ContentType
import msocket.api.ContentType.Json
import msocket.http.post.ClientHttpCodecs
import msocket.http.post.streaming.FetchEvent
import msocket.http.ws.WebsocketExtensions.WebsocketEncoding
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration.DurationLong

class ServerRouteTests
    extends AnyFunSuite
    with ScalatestRouteTest
    with Matchers
    with ExampleCodecs
    with ClientHttpCodecs
    with BeforeAndAfterAll {

  override def clientContentType: ContentType = Json

  private val wiring = new ServerWiring {
    override implicit lazy val actorSystem: ActorSystem[?] = system.toTyped
  }

  protected override def afterAll(): Unit = {
    wiring.actorSystem.terminate()
    Await.result(wiring.actorSystem.whenTerminated, 10.seconds)
  }

  test("websocket") {
    val wsClient = WSProbe()

    WS(s"/websocket-endpoint", wsClient.flow) ~> wiring.exampleServer.routesWithCors ~> check {
      wsClient.sendMessage(Json.strictMessage(GetNumbers(3): ExampleStreamRequest))
      isWebSocketUpgrade shouldBe true
      wsClient.expectMessage() shouldBe TextMessage.Strict("3")
      wsClient.expectMessage() shouldBe TextMessage.Strict("6")
      wsClient.expectMessage() shouldBe TextMessage.Strict("9")
      wsClient.expectMessage() shouldBe TextMessage.Strict("12")
      wsClient.expectMessage() shouldBe TextMessage.Strict("15")
    }
  }

  test("http-streaming") {
    implicit val timeout: RouteTestTimeout = RouteTestTimeout(10.seconds.dilated)
    Post("/post-streaming-endpoint", HelloStream("mushtaq"): ExampleStreamRequest) ~> wiring.exampleServer.routesWithCors ~> check {
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
