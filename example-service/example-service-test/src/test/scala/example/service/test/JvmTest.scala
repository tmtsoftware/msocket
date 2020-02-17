package example.service.test

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.stream.scaladsl.Sink
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.testkit.scaladsl.TestSink
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import msocket.api.ContentType.Json
import msocket.api.Transport
import msocket.example.server.ServerWiring
import msocket.impl.post.HttpPostTransport
import msocket.impl.ws.WebsocketTransport
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration.DurationLong

class JvmTest extends AnyFunSuite with Matchers with BeforeAndAfterAll with ExampleCodecs with ScalaFutures {
  val wiring = new ServerWiring()

  import wiring._
  val httpPostTransport = new HttpPostTransport[ExampleRequest]("http://localhost:1111/post-endpoint", Json, () => None)
  val websocketTransport: Transport[ExampleRequest] =
    new WebsocketTransport[ExampleRequest]("ws://localhost:1111/websocket-endpoint", Json).logRequest().logRequestResponse()

  override protected def beforeAll(): Unit = {
    Await.result(exampleServer.start("0.0.0.0", 1111), 5.seconds)
  }

  override protected def afterAll(): Unit = {
    Await.result(exampleServer.stop(), 5.seconds)
  }

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = 3.seconds)

  def probe[T](implicit system: ActorSystem[_]): Sink[T, Probe[T]] = TestSink.probe[T](system.toClassic)

  test("return response for request using http transport"){
    lazy val httpPostTransport =
      new HttpPostTransport[ExampleRequest]("http://localhost:1111/post-endpoint", Json, () => None)
    val client   = new ExampleClient(httpPostTransport)
    client.hello("John").futureValue shouldBe "Hello John"
  }

  test("expects streaming response and a subscription for request using http transport") {
    val client = new ExampleClient(httpPostTransport)
    client
      .getNumbers(12)
      .runWith(probe)
      .request(2)
      .expectNext(12)
      .expectNext(24)
  }

  test("expects subscription for request using http transport") {
    val client = new ExampleClient(httpPostTransport)
    client
      .getNumbers(12)
      .runWith(probe)
      .expectSubscription()
  }
  test("expects streaming response and a subscription for request using websocket transport") {
    val client = new ExampleClient(websocketTransport)
    client
      .getNumbers(12)
      .runWith(probe)
      .request(2)
      .expectNextN(Seq(12, 24))
  }
  //  test("should be able cancel using subscription of streaming response") {}
}
