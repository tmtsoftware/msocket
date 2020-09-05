package msocket.impl.rsocket.client

import io.rsocket.RSocket
import io.rsocket.core.RSocketConnector
import io.rsocket.transport.ClientTransport
import msocket.api.ContentType

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.Future

object RSocketFactoryS {
  def client(clientTransport: ClientTransport, contentType: ContentType): Future[RSocket] = {
    RSocketConnector
      .create()
      .dataMimeType(contentType.mimeType)
      .connect(clientTransport)
      .toFuture
      .toScala
  }
}
