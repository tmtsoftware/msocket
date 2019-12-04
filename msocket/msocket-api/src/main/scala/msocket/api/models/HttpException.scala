package msocket.api.models

case class HttpException(statusCode: Int, reason: String, message: String)
    extends RuntimeException(
      s"""
         |StatusCode :  $statusCode
         |Reason     :  $reason
         |Message    :  $message
         |""".stripMargin
    )
