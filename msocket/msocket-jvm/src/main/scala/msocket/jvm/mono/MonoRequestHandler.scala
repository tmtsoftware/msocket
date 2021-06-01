package msocket.jvm.mono

import io.bullet.borer.Encoder
import msocket.security.api.AuthorizationPolicy
import msocket.security.models.AccessToken

import scala.concurrent.Future

trait MonoRequestHandler[Req] {
  def handle(request: Req): Future[MonoResponse]

  protected def response[Res: Encoder](result: Future[Res]): Future[MonoResponse] =
    Future.successful(MonoResponse.from(_ => result, None))

  protected def sResponse[Res: Encoder](policy: AuthorizationPolicy)(resultFactory: AccessToken => Future[Res]): Future[MonoResponse] =
    Future.successful(MonoResponse.from(resultFactory, Some(policy)))
}
