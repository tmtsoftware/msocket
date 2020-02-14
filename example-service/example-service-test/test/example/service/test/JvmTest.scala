package example.service.test

import csw.example.api.client.ExampleClient
import csw.example.api.protocol.{ExampleCodecs, ExampleRequest}
import msocket.api.ContentType.Json
import msocket.example.server.ServerWiring
import msocket.impl.post.HttpPostTransport
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class JvmTest extends AsyncFlatSpec with BeforeAndAfterAll with Matchers with ExampleCodecs {
  val wiring = new ServerWiring()
  import wiring.actorSystem
  override protected def beforeAll(): Unit = {
    wiring.exampleServer.start("0.0.0.0", 1111)
  }

  it should "return response for request using http transport" in {
    lazy val httpPostTransport =
      new HttpPostTransport[ExampleRequest]("http://0.0.0.0:1111/post-endpoint", Json, () => None)
    val client   = new ExampleClient(httpPostTransport)
    val response = client.hello("John")
    response map { value =>
      assert(value == "Hello John")
    }
  }

  override protected def afterAll(): Unit = {
    wiring.exampleServer.stop()
  }
}
