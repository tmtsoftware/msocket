package msocket.impl.extensions

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.github.ghik.silencer.silent

object SourceExtensions {

  implicit class SourceWithSubscribe[Out, Mat](x: Source[Out, Mat]) {
    def subscribe(f: Out => Unit)(implicit @silent mat: Materializer): Mat = {
      x.foreach(f)
      x.materializedValue
    }
  }

}
