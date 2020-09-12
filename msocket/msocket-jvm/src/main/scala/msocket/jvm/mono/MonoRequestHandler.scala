package msocket.jvm.mono

import io.bullet.borer.Encoder
import msocket.security.api.{AuthorizationPolicy, PassThroughPolicy}

import scala.concurrent.Future

trait MonoRequestHandler[Req] {
  def handle(request: Req): Future[MonoResponse]

  protected def future[Res: Encoder](result: Future[Res]): Future[MonoResponse] = sFuture(PassThroughPolicy)(result)

  protected def sFuture[Res: Encoder](policy: AuthorizationPolicy)(result: => Future[Res]): Future[MonoResponse] =
    Future.successful(MonoResponse.from(result, policy))
}
