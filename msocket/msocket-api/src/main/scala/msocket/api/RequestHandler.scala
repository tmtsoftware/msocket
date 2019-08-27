package msocket.api

trait RequestHandler[Req, Res] {
  def handle(request: Req): Res
}
