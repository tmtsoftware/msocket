package msocket.impl.post

import akka.NotUsed
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import io.bullet.borer.compat.AkkaHttpCompat
import msocket.api.codecs.BasicCodecs

import scala.reflect.ClassTag

object ServerHttpCodecs extends ServerHttpCodecs
trait ServerHttpCodecs extends AkkaHttpCompat with BasicCodecs {
  val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport
    .json(8 * 1024)
    .withFramingRenderer(Flow[ByteString].intersperse(ByteString("\n")))

  override implicit def borerJsonStreamToEntityMarshaller[T: ToEntityMarshaller: ClassTag]: ToEntityMarshaller[Source[T, NotUsed]] =
    borerStreamMarshaller[T](jsonStreamingSupport)

}
