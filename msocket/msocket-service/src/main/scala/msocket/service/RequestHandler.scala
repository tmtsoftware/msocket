package msocket.service

trait RequestHandler[Req, Res] {
  def handle(request: Req): Res
}
