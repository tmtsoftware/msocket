package msocket.http.post

import org.apache.pekko.NotUsed
import org.apache.pekko.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import org.apache.pekko.http.scaladsl.marshalling.ToEntityMarshaller
import org.apache.pekko.stream.scaladsl.{Flow, Source}
import org.apache.pekko.util.ByteString
import io.bullet.borer.compat.PekkoHttpCompat
import msocket.api.codecs.BasicCodecs

import scala.reflect.ClassTag

object ServerHttpCodecs extends ServerHttpCodecs
trait ServerHttpCodecs  extends PekkoHttpCompat with BasicCodecs {
  val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport
    .json(8 * 1024)
    .withFramingRenderer(Flow[ByteString].intersperse(ByteString("\n")))

  override implicit def borerJsonStreamToEntityMarshaller[T: ToEntityMarshaller: ClassTag]: ToEntityMarshaller[Source[T, NotUsed]] =
    borerStreamMarshaller[T](jsonStreamingSupport)

}
