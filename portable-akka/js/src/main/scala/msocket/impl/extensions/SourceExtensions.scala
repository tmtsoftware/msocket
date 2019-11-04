package msocket.impl.extensions

import akka.stream.scaladsl.Source

object SourceExtensions {

  implicit class SourceWithSubscribe[Out, Mat](x: Source[Out, Mat]) {
    def subscribe(f: Out => Unit): Mat = {
      x.runForeach(f)
      x.materializedValue
    }
  }

}
