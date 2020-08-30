package msocket.example.client

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.ExampleCodecs
import csw.example.api.protocol.ExampleError.{GetNumbersError, HelloError}
import csw.example.api.protocol.ExampleProtocol.{ExampleRequest, ExampleStreamRequest}
import msocket.api.ContentType.{Cbor, Json}
import msocket.api.Subscription
import msocket.api.models.ServiceError
import msocket.impl.post.HttpPostTransportJs
import msocket.impl.rsocket.RSocketTransportFactoryJs
import msocket.impl.sse.SseTransportJs
import msocket.impl.ws.WebsocketTransportJs
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.async.Async._
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Promise}

class JsTest extends AsyncFreeSpec with Matchers with ExampleCodecs with TestPolyfills {

  val PostEndpoint          = "http://localhost:5000/post-endpoint"
  val PostStreamingEndpoint = "http://localhost:5000/post-streaming-endpoint"
  val SseEndpoint           = "http://localhost:5000/sse-endpoint"
  val WebsocketEndpoint     = "ws://localhost:5000/websocket-endpoint"
  val RSocketEndpoint       = "ws://localhost:7000"

  implicit val streamingDelay: FiniteDuration              = 1.second
  implicit val actorSystem: ActorSystem[Any]               = new ActorSystem
  override implicit def executionContext: ExecutionContext = actorSystem.executionContext

  List(Json, Cbor).foreach { contentType =>
    lazy val httpResponseTransport = new HttpPostTransportJs[ExampleRequest](PostEndpoint, contentType)
    lazy val httpStreamTransport   = new HttpPostTransportJs[ExampleStreamRequest](PostStreamingEndpoint, contentType)

    lazy val rSocketResponseTransport = new RSocketTransportFactoryJs[ExampleRequest].connect(RSocketEndpoint, contentType)
    lazy val rSocketStreamTransport   = new RSocketTransportFactoryJs[ExampleStreamRequest].connect(RSocketEndpoint, Json)

    lazy val sseTransport       = new SseTransportJs[ExampleStreamRequest](SseEndpoint)
    lazy val websocketTransport = new WebsocketTransportJs[ExampleStreamRequest](WebsocketEndpoint, contentType)

    contentType.toString - {
      "requestResponse" - {
        List(httpResponseTransport, rSocketResponseTransport).foreach { transport =>
          transport.getClass.getSimpleName - {
            "success response" in async {
              val client = new ExampleClient(transport, null)
              await(client.hello("John")) shouldBe "Hello John"
            }

            "domain error" in async {
              val client = new ExampleClient(transport, null)
              val caught = await {
                recoverToExceptionIf[HelloError] {
                  client.hello("idiot")
                }
              }

              caught shouldBe HelloError(5)
            }

            "generic error" in async {
              val client = new ExampleClient(transport, null)
              val caught = await {
                recoverToExceptionIf[ServiceError] {
                  client.hello("fool")
                }
              }
              caught shouldBe ServiceError.fromThrowable(new IllegalArgumentException("you are a fool"))
            }
          }
        }
      }

      "requestStream" - {
        lazy val bilingualTransports = List(rSocketStreamTransport, websocketTransport)
        lazy val jsonOnlyTransports  = List(sseTransport, httpStreamTransport)
        val transports               = if (contentType == Json) bilingualTransports ++ jsonOnlyTransports else bilingualTransports

        transports.foreach { transport =>
          transport.getClass.getSimpleName - {
            s"simple stream" in async {
              val client                            = new ExampleClient(null, transport)
              val stream: Source[Int, Subscription] = client.getNumbers(12)
              val subscription                      = stream.materializedValue
              val p                                 = Promise[Done]()
              var list                              = Seq.empty[Int]
              stream.onMessage { x =>
                list :+= x
                if (list.length == 2) {
                  p.success(Done)
                  subscription.cancel()
                }
              }

              await(p.future) shouldBe Done
              list shouldBe List(12, 24)
            }

            "domain error " in async {
              val client                            = new ExampleClient(null, transport)
              val stream: Source[Int, Subscription] = client.getNumbers(-1)
              val subscription                      = stream.materializedValue
              val p                                 = Promise[Done]()

              stream.onError { err =>
                err shouldBe GetNumbersError(17)
                p.success(Done)
                subscription.cancel()
              }

              await(p.future) shouldBe Done
            }

            "generic error " in async {
              val client                            = new ExampleClient(null, transport)
              val stream: Source[Int, Subscription] = client.getNumbers(0)
              val subscription                      = stream.materializedValue
              val p                                 = Promise[Done]()

              stream.onError { err =>
                err shouldBe ServiceError.fromThrowable(new ArithmeticException("/ by zero"))
                p.success(Done)
                subscription.cancel()
              }

              await(p.future) shouldBe Done
            }
          }

        }
      }
    }
  }
}
