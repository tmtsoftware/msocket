package example.service.test

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import msocket.api.ContentType.Json
import msocket.impl.post.HttpPostTransport
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import msocket.example.server.ServerWiring

import scala.concurrent.{ExecutionContextExecutor, Future}

class JvmTest extends AsyncFlatSpec with ExampleCodecs with BeforeAndAfterAll with Matchers {
  val wiring = new ServerWiring()
  override protected def beforeAll(): Unit = {
    Future {
      wiring.exampleServer.startServer("0.0.0.0", 1111)
    }(wiring.ec)
  }

  it should "return response for request using http transport" in {
    implicit lazy val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "test")
    implicit val ec: ExecutionContextExecutor      = system.executionContext

    lazy val httpPostTransport =
      new HttpPostTransport[ExampleRequest]("http://0.0.0.0:1111/post-endpoint", Json, () => None)
    val client   = new ExampleClient(httpPostTransport)
    val response = client.hello("John")
    response map { value =>
      assert(value == "Hello John")
    }
  }

  override protected def afterAll(): Unit = {
    wiring.actorSystem.terminate()
  }
}
