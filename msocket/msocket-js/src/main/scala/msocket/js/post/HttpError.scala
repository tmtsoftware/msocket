package msocket.js.post

case class HttpError(statusCode: Int, reason: String, message: String)
    extends RuntimeException(
      s"""
         |StatusCode :  $statusCode
         |Reason     :  $reason
         |Message    :  $message
         |""".stripMargin
    )
