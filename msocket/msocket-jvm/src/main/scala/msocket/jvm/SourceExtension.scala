package msocket.jvm

import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Source}
import msocket.api.Subscription

object SourceExtension {
  implicit class WithSubscription[Out, Mat](stream: Source[Out, Mat]) {
    def withSubscription(): Source[Out, Subscription] = {
      stream
        .viaMat(KillSwitches.single)(Keep.right)
        .mapMaterializedValue[Subscription](switch => () => switch.shutdown())
    }
  }
}
