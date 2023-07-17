package msocket.jvm

import org.apache.pekko.stream.KillSwitches
import org.apache.pekko.stream.scaladsl.{Keep, Source}
import msocket.api.Subscription

object SourceExtension {
  implicit class RichSource[Out, Mat](stream: Source[Out, Mat]) {
    def withSubscription(): Source[Out, Subscription] = {
      stream
        .viaMat(KillSwitches.single)(Keep.right)
        .mapMaterializedValue[Subscription](switch => () => switch.shutdown())
    }

    def distinctUntilChanged: Source[Out, Mat] =
      stream
        .map(Option.apply)
        .prepend(Source.single(None))
        .sliding(2)
        .collect { case Seq(a, b @ Some(x)) if a != b => x }
  }
}
