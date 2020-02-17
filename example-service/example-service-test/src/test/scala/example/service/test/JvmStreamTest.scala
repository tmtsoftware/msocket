import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.model.ws.WebSocketRequest
import akka.stream.testkit.scaladsl.TestSink
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import msocket.api.ContentType.Json
import msocket.example.server.ServerWiring
import msocket.impl.post.HttpPostTransport
import msocket.impl.ws.WebsocketTransport
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class JvmStreamTest extends AnyFunSuite with Matchers with BeforeAndAfterAll with ExampleCodecs {
  val wiring = new ServerWiring()

  import wiring._
  lazy val httpPostTransport = new HttpPostTransport[ExampleRequest]("http://0.0.0.0:1111/post-endpoint", Json, () => None)
  lazy val websocketTransport =
    new WebsocketTransport[ExampleRequest]("ws://localhost:1111/websocket-endpoint", Json).logRequest().logRequestResponse()
  private val classic: ActorSystem = wiring.actorSystem.toClassic
  override protected def beforeAll(): Unit = {
    wiring.exampleServer.start("0.0.0.0", 1111)
  }

  test("expects streaming response and a subscription for request using http transport") {
    val client = new ExampleClient(httpPostTransport)
    client
      .getNumbers(12)
      .runWith(TestSink.probe[Int](classic))
      .request(2)
      .expectNextN(Seq(12, 24))
  }

  test("expects subscription for request using http transport") {
    val client = new ExampleClient(httpPostTransport)
    client
      .getNumbers(12)
      .runWith(TestSink.probe[Int](classic))
      .expectSubscription()
  }
  test("expects streaming response and a subscription for request using websocket transport") {
    val client = new ExampleClient(websocketTransport)
    client
      .getNumbers(12)
      .runWith(TestSink.probe[Int](classic))
      .request(2)
      .expectNextN(Seq(12, 24))
  }
  //  test("should be able cancel using subscription of streaming response") {}

  override protected def afterAll(): Unit = {
    wiring.exampleServer.stop()
  }
}
