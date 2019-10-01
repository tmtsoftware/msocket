package msocket.api.models

case class HttpException(statusCode: Int, reason: String, message: String)
    extends RuntimeException(s"statusCode:$statusCode, reason:$reason, message: $message")
