package msocket.rsocket.client

import io.rsocket.RSocket
import io.rsocket.core.RSocketConnector
import io.rsocket.transport.ClientTransport
import msocket.api.ContentType

import scala.jdk.FutureConverters.*
import scala.concurrent.Future

object RSocketFactoryS {
  def client(clientTransport: ClientTransport, contentType: ContentType): Future[RSocket] = {
    RSocketConnector
      .create()
      .dataMimeType(contentType.mimeType)
      .connect(clientTransport)
      .toFuture
      .asScala
  }
}
