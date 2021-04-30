package msocket.jvm

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.{Sink, Source}
import msocket.jvm.SourceExtension.RichSource
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class SourceExtensionTest extends AnyWordSpec with Matchers {

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "test")
  "distinctUntilChanged" in {
    Await.result(Source(List(1, 2, 2, 3, 3, 3, 4, 1, 5, 5, 5, 4, 3, 4)).distinctUntilChanged.runWith(Sink.seq), 1.second) shouldBe List(
      1, 2, 3, 4, 1, 5, 4, 3, 4
    )
  }
}
