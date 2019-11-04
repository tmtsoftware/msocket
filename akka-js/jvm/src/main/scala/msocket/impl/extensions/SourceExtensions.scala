package msocket.impl.extensions

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}

object SourceExtensions {

  implicit class SourceWithSubscribe[Out, Mat](x: Source[Out, Mat]) {
    def subscribe(f: Out => Unit)(implicit mat: Materializer): Mat = {
      x.to(Sink.foreach(f)).run()
    }
  }

}
