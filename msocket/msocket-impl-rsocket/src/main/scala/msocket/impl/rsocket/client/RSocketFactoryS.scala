package msocket.impl.rsocket.client

import io.rsocket.transport.ClientTransport
import io.rsocket.{RSocket, RSocketFactory}
import msocket.api.ContentType

import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.Future

object RSocketFactoryS {
  def client(clientTransport: ClientTransport, contentType: ContentType): Future[RSocket] = {
    RSocketFactory.connect
      .dataMimeType(contentType.mimeType)
      .transport(clientTransport)
      .start
      .toFuture
      .toScala
  }
}
