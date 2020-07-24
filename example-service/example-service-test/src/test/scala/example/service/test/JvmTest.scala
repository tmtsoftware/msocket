package example.service.test

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.testkit.scaladsl.TestSink
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleError.{GetNumbersError, HelloError}
import csw.example.api.protocol.ExampleRequest.{ExampleRequestResponse, ExampleRequestStream}
import msocket.api.ContentType.{Cbor, Json}
import msocket.api.Subscription
import msocket.api.models.ServiceError
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

  Await.result(exampleServer.start("0.0.0.0", 5000), 10.seconds)
  Await.result(rSocketServer.start("0.0.0.0", 7000), 10.seconds)

  override protected def afterAll(): Unit = {
    Await.result(rSocketServer.stop(), 10.seconds)
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 10.seconds)
  }

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = 30.seconds)

  def makeProbe[T](implicit system: ActorSystem[_]): Sink[T, Probe[T]] = TestSink.probe[T](system.toClassic)

  val PostEndpoint          = "http://localhost:5000/post-endpoint"
  val PostStreamingEndpoint = "http://localhost:5000/post-streaming-endpoint"
  val SseEndpoint           = "http://localhost:5000/sse-endpoint"
  val WebsocketEndpoint     = "ws://localhost:5000/websocket-endpoint"
  val RSocketEndpoint       = "ws://localhost:7000"

  List(Json, Cbor).foreach { contentType =>
    lazy val httpResponseTransport = new HttpPostTransport[ExampleRequestResponse](PostEndpoint, contentType, () => None)
    lazy val httpStreamTransport   = new HttpPostTransport[ExampleRequestStream](PostStreamingEndpoint, contentType, () => None)

    lazy val rSocketResponseTransport = new RSocketTransportFactory[ExampleRequestResponse].transport(RSocketEndpoint, contentType)
    lazy val rSocketStreamTransport   = new RSocketTransportFactory[ExampleRequestStream].transport(RSocketEndpoint, contentType)

    lazy val sseTransport       = new SseTransport[ExampleRequestStream](SseEndpoint)
    lazy val websocketTransport = new WebsocketTransport[ExampleRequestStream](WebsocketEndpoint, contentType)

    List(httpResponseTransport, rSocketResponseTransport).foreach { transport =>
      s"${transport.getClass.getSimpleName} and ${contentType.toString}" must {
        s"requestResponse" in {
          val client = new ExampleClient(transport, null)
          client.hello("John").futureValue shouldBe "Hello John"
        }

        s"requestResponse with domain error" in {
          val client = new ExampleClient(transport, null)
          val caught = intercept[HelloError] {
            Await.result(client.hello("idiot"), 3.second)
          }
          caught shouldBe HelloError(5)
        }

        s"requestResponse expect generic error on fool" in {
          val client = new ExampleClient(transport, null)
          val caught = intercept[ServiceError] {
            Await.result(client.hello("fool"), 3.second)
          }
          caught shouldBe ServiceError.fromThrowable(new IllegalArgumentException("you are a fool"))
        }
      }
    }

    val bilingualTransports = List(rSocketStreamTransport, websocketTransport)
    val jsonOnlyTransports  = List(sseTransport, httpStreamTransport)
    val transports          = if (contentType == Json) bilingualTransports ++ jsonOnlyTransports else bilingualTransports

    transports.foreach { transport =>
      s"${transport.getClass.getSimpleName} and ${contentType.toString}" must {
        s"requestStream" in {
          val client = new ExampleClient(null, transport)
          client
            .getNumbers(12)
            .runWith(makeProbe)
            .request(2)
            .expectNextN(Seq(12, 24))
        }

        s"requestStream with interval" in {
          val client = new ExampleClient(null, transport)
          client
            .helloStream("John")
            .runWith(makeProbe)
            .request(2)
            .expectNext("hello \n John again 0")
            .expectNoMessage(100.millis)
            .expectNext("hello \n John again 1")
        }

        s"requestStream with domain error " in {
          val client = new ExampleClient(null, transport)
          client
            .getNumbers(-1)
            .runWith(makeProbe)
            .request(1)
            .expectError(GetNumbersError(17))
        }

        s"requestStream subscription with cancellation " in {
          val client                                            = new ExampleClient(null, transport)
          val source: Source[Int, Subscription]                 = client.getNumbers(3)
          val (subscription, probe): (Subscription, Probe[Int]) = source.toMat(makeProbe)(Keep.both).run()
          probe.request(2)
          subscription.cancel()
          probe.expectComplete()
        }
      }
    }
  }
}
