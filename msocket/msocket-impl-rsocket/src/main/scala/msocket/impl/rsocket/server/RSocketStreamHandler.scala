package msocket.impl.rsocket.server

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Dom.Element
import io.bullet.borer.Encoder
import io.rsocket.Payload
import msocket.api.models.ServiceError
import msocket.api.{ContentType, ErrorProtocol}
import msocket.impl.StreamHandler
import msocket.impl.rsocket.RSocketExtensions._

/**
 * This helper class can be extended to define custom RSocket handler in the server which returns [[Source]] of [[Payload]].
 * RSocketStreamHandler takes a request type which will be bound to Domain specific error using ErrorProtocol.
 */
abstract class RSocketStreamHandler[Req: ErrorProtocol](contentType: ContentType) extends StreamHandler[Req, Payload] {
  override def encode[Res: Encoder](response: Res): Payload = contentType.payload(response)
}

object RSocketStreamHandler {
  val Missing: ContentType => RSocketStreamHandler[Element] = { contentType =>
    new RSocketStreamHandler[Element](contentType)(ErrorProtocol.bind[Element, ServiceError]) {
      override def handle(request: Element): Source[Payload, NotUsed] = Source.failed(new RuntimeException("missing stream handler"))
    }
  }
}
