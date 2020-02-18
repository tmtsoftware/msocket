package example.service.test

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.stream.scaladsl.Sink
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.testkit.scaladsl.TestSink
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import msocket.api.ContentType.{Cbor, Json}
import msocket.example.server.ServerWiring
import msocket.impl.post.HttpPostTransport
import msocket.impl.rsocket.client.RSocketTransportFactory
import msocket.impl.sse.SseTransport
import msocket.impl.ws.WebsocketTransport
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration.DurationLong

class JvmTest
    extends AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with ExampleCodecs
    with ScalaFutures
    with TableDrivenPropertyChecks {
  val wiring = new ServerWiring()

  import wiring._

  Await.result(rSocketServer.start("0.0.0.0", 7000), 50.seconds)
  Await.result(exampleServer.start("0.0.0.0", 1111), 50.seconds)

  override protected def afterAll(): Unit = {
    rSocketServer.stop()
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 5.seconds)
  }

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = 30.seconds)

  def probe[T](implicit system: ActorSystem[_]): Sink[T, Probe[T]] = TestSink.probe[T](system.toClassic)

  List(Json, Cbor).foreach { contentType =>
    val httpPostTransport  = new HttpPostTransport[ExampleRequest]("http://localhost:1111/post-endpoint", contentType, () => None)
    val websocketTransport = new WebsocketTransport[ExampleRequest]("ws://localhost:1111/websocket-endpoint", contentType)
    val rSocketTransport   = new RSocketTransportFactory[ExampleRequest].transport("ws://localhost:7000", contentType)
    val sseTransport       = new SseTransport[ExampleRequest]("http://localhost:1111/sse-endpoint")

    List(httpPostTransport, rSocketTransport).foreach { transport =>
      s"${transport.getClass.getSimpleName} and ${contentType.toString}" must {
        s"requestResponse for hello API" in {
          val client = new ExampleClient(transport)
          client.hello("John").futureValue shouldBe "Hello John"
        }

        s"requestResponse for randomBag API" in {
          val client    = new ExampleClient(transport)
          val randomBag = client.randomBag().futureValue
          randomBag.red should be > 0
          randomBag.red should be < 10
          randomBag.blue should be > 0
          randomBag.blue should be < 10
          randomBag.green should be > 0
          randomBag.green should be < 10
        }
      }
    }

    s"websocketTransport and ${contentType.toString} contentType" must {
      "requestResponse without timeout should give error" in {
        val client: ExampleClient = new ExampleClient(websocketTransport)
        val caught = intercept[RuntimeException] {
          client.hello("John").futureValue
        }
        caught.getCause.getMessage shouldBe "requestResponse protocol without timeout is not supported for this transport"
      }
    }

    val bilingualTransports = List(rSocketTransport, websocketTransport)
    val transports          = if (contentType == Json) bilingualTransports :+ sseTransport :+ httpPostTransport else bilingualTransports

    transports.foreach { transport =>
      s"${transport.getClass.getSimpleName} and ${contentType.toString}" must {
        s"requestStream for getNumbers API" in {
          val client = new ExampleClient(transport)
          client
            .getNumbers(12)
            .runWith(probe)
            .request(2)
            .expectNextN(Seq(12, 24))
        }

        s"requestStream for helloStream API" in {
          val client = new ExampleClient(transport)
          client
            .helloStream("John")
            .runWith(probe)
            .request(2)
            .expectNext("hello \n John again 0")
            .expectNoMessage(100.millis)
            .expectNext("hello \n John again 1")
        }
      }
    }
  }
}
