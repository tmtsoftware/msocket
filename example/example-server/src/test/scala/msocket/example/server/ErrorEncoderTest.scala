package msocket.example.server

import csw.example.api.protocol.{ExampleCodecs, ExampleError}
import io.bullet.borer.Dom.{Element, StringElem}
import io.bullet.borer.{Cbor, Json}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ErrorEncoderTest extends AnyFreeSpec with Matchers {
  import ExampleCodecs.{exampleRequestResponseErrorProtocol => ep}
  import ExampleError._

  List(HelloError(55), GetNumbersError(99) /*, ErrorWithEmptyConstructor()*/ ).foreach {
    case ex: ep.E =>
      ex.getClass.getSimpleName - {
        List(Json, Cbor).foreach { target =>
          target.toString in {
            val bytes           = target.encode(ex).toByteArray
            val domainException = target.decode(bytes).to[ep.E].value
            val dom             = target.decode(bytes).to[Map[String, Element]].value
            domainException shouldBe ex
            dom("error_message") shouldBe StringElem(ex.getMessage)
          }
        }
      }
    case x        => throw new MatchError(x)
  }
}
