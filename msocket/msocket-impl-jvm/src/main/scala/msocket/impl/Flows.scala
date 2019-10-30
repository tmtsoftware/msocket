package msocket.impl

import java.time.LocalTime

import akka.NotUsed
import akka.stream.scaladsl.Flow

import scala.concurrent.ExecutionContext

object Flows {

  def logTermination[T](msg: String)(implicit ec: ExecutionContext): Flow[T, T, NotUsed] = Flow[T].watchTermination() {
    case (x, doneF) =>
      doneF.onComplete(status => println(s"$msg at  ${LocalTime.now()} with status = $status"))
      x
  }

}
