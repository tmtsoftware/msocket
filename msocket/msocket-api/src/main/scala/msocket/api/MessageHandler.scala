package msocket.api

trait MessageHandler[In, Out] {
  def handle(request: In): Out
}
