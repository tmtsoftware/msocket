package msocket.impl

import akka.NotUsed
import akka.stream.scaladsl.Source
import io.bullet.borer.Encoder

import scala.concurrent.Future

trait StreamExtensions[M] {
  def stream[T, Mat](input: Source[T, Mat])(implicit encoder: Encoder[T]): Source[M, NotUsed]
  def futureAsStream[T](input: Future[T])(implicit encoder: Encoder[T]): Source[M, NotUsed] = stream(Source.future(input))
}
