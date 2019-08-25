package msocket.api

trait PostHandler[Req, Res] {
  def handle(request: Req): Res
}
