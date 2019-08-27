package mscoket.impl

import akka.NotUsed
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.{Marshaller, PredefinedToResponseMarshallers, ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentTypeRange, MediaType}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import akka.stream.scaladsl.{Flow, Keep, Source}
import akka.util.ByteString
import io.bullet.borer.compat.akka._
import io.bullet.borer.{Decoder, Encoder, Json}

import scala.collection.immutable.Seq
import scala.concurrent.Future

trait HttpCodecs {

  lazy val mediaTypes: Seq[MediaType.WithFixedCharset]     = List(`application/json`)
  lazy val unmarshallerContentTypes: Seq[ContentTypeRange] = mediaTypes.map(ContentTypeRange.apply)

  implicit def liftMarshaller[T](implicit m: ToEntityMarshaller[T]): ToResponseMarshaller[T] =
    PredefinedToResponseMarshallers.fromToEntityMarshaller()

  implicit def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] = {
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map(Json.decode(_).to[A].value)
  }

  implicit def marshaller[A: Encoder]: ToEntityMarshaller[A] = {
    Marshaller
      .oneOf(mediaTypes: _*)(Marshaller.byteStringMarshaller(_))
      .compose(Json.encode(_).to[ByteString].result)
  }

  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  implicit def borerJsonSourceReader[T: Decoder](implicit support: EntityStreamingSupport): FromEntityUnmarshaller[Source[T, NotUsed]] =
    Unmarshaller.withMaterializer { implicit ec => mat => e =>
      if (support.supported.matches(e.contentType)) {
        val frames = e.dataBytes.via(support.framingDecoder)

        def unmarshal(byteString: ByteString) = Future(Json.decode(byteString).to[T].value)
        val unmarshallingFlow = if (support.unordered) {
          Flow[ByteString].mapAsyncUnordered(support.parallelism)(unmarshal)
        } else {
          Flow[ByteString].mapAsync(support.parallelism)(unmarshal)
        }

        FastFuture.successful(frames.viaMat(unmarshallingFlow)(Keep.right))
      } else {
        FastFuture.failed(Unmarshaller.UnsupportedContentTypeException(support.supported))
      }
    }
}
