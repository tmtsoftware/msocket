package msocket.impl.post

import akka.NotUsed
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import io.bullet.borer._
import io.bullet.borer.compat.AkkaHttpCompat

trait ServerHttpCodecs extends AkkaHttpCompat {
  val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport
    .json(8 * 1024)
    .withFramingRenderer(Flow[ByteString].intersperse(ByteString("\n")))

  override implicit def borerJsonStreamFromEntityUnmarshaller[T: Decoder]: FromEntityUnmarshaller[Source[T, NotUsed]] =
    borerStreamUnmarshaller(jsonStreamingSupport)
}
