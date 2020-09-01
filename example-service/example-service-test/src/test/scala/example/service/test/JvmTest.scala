package example.service.test

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.TestSubscriber.Probe
import akka.stream.testkit.scaladsl.TestSink
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleError.{GetNumbersError, HelloError}
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, ExampleStreamRequest}
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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

import scala.concurrent.Await
import scala.concurrent.duration.DurationLong

class JvmTest extends AnyFreeSpec with Matchers with BeforeAndAfterAll with ExampleCodecs with ScalaFutures with TableDrivenPropertyChecks {

  val wiring = new ServerWiring()

  import wiring._

  Await.result(exampleServer.start("0.0.0.0", 5000), 10.seconds)
  Await.result(rSocketServer.start("0.0.0.0", 7000), 10.seconds)
  var connections: List[Subscription] = Nil

  override protected def afterAll(): Unit = {
    connections.foreach(_.cancel())
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
    lazy val httpResponseTransport = new HttpPostTransport[ExampleRequest](PostEndpoint, contentType, () => None)
    lazy val httpStreamTransport   = new HttpPostTransport[ExampleStreamRequest](PostStreamingEndpoint, contentType, () => None)

    lazy val (rSocketResponseTransport, connection1) = RSocketTransportFactory.connect[ExampleRequest](RSocketEndpoint, contentType)
    lazy val (rSocketStreamTransport, connection2)   = RSocketTransportFactory.connect[ExampleStreamRequest](RSocketEndpoint, contentType)

    connections :::= List(connection1, connection2)

    lazy val sseTransport       = new SseTransport[ExampleStreamRequest](SseEndpoint)
    lazy val websocketTransport = new WebsocketTransport[ExampleStreamRequest](WebsocketEndpoint, contentType)

    contentType.toString - {
      "requestResponse" - {
        List(httpResponseTransport, rSocketResponseTransport).foreach { transport =>
          transport.getClass.getSimpleName - {
            "success response" in {
              val client = new ExampleClient(transport, null)
              client.hello("John").futureValue shouldBe "Hello John"
            }

            "domain error" in {
              val client = new ExampleClient(transport, null)
              val caught = intercept[HelloError] {
                Await.result(client.hello("idiot"), 3.second)
              }
              caught shouldBe HelloError(5)
            }

            "generic error" in {
              val client = new ExampleClient(transport, null)
              val caught = intercept[ServiceError] {
                Await.result(client.hello("fool"), 3.second)
              }
              caught shouldBe ServiceError.fromThrowable(new IllegalArgumentException("you are a fool"))
            }
          }
        }
      }

      "requestStream" - {
        val bilingualTransports = List(rSocketStreamTransport, websocketTransport)
        val jsonOnlyTransports  = List(sseTransport, httpStreamTransport)
        val transports          = if (contentType == Json) bilingualTransports ++ jsonOnlyTransports else bilingualTransports

        transports.foreach { transport =>
          transport.getClass.getSimpleName - {
            s"simple stream" in {
              val client = new ExampleClient(null, transport)
              client
                .getNumbers(12)
                .runWith(makeProbe)
                .request(2)
                .expectNextN(Seq(12, 24))
            }

            "stream with interval" in {
              val client = new ExampleClient(null, transport)
              client
                .helloStream("John")
                .runWith(makeProbe)
                .request(2)
                .expectNext("hello \n John again 0")
                .expectNoMessage(100.millis)
                .expectNext("hello \n John again 1")
            }

            "domain error " in {
              val client = new ExampleClient(null, transport)
              client
                .getNumbers(-1)
                .runWith(makeProbe)
                .request(1)
                .expectError(GetNumbersError(17))
            }

            "stream cancellation " in {
              val client                            = new ExampleClient(null, transport)
              val source: Source[Int, Subscription] = client.getNumbers(3)
              val (subscription, countF)            = source.toMat(Sink.fold(0)((acc, _) => acc + 1))(Keep.both).run()
              subscription.cancel()
              val count                             = Await.result(countF, 1.seconds)
              // if stream cancels successfully, count will be finite, in most cases 0
              count should be < Int.MaxValue
            }
          }
        }
      }
    }
  }
}
