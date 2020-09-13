package msocket.jvm.mono

import io.bullet.borer.Encoder
import msocket.security.api.{AuthorizationPolicy, PassThroughPolicy}
import msocket.security.models.AccessToken

import scala.concurrent.Future

trait MonoRequestHandler[Req] {
  def handle(request: Req): Future[MonoResponse]

  protected def future[Res: Encoder](result: Future[Res]): Future[MonoResponse] =
    sFuture(PassThroughPolicy)(_ => result)

  protected def sFuture[Res: Encoder](policy: AuthorizationPolicy)(resultFactory: AccessToken => Future[Res]): Future[MonoResponse] =
    Future.successful(MonoResponse.from(resultFactory, policy))
}
