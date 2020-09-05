package msocket.jvm

trait RequestHandler[Req, Res] {
  def handle(request: Req): Res
}
