package msocket.api

import io.bullet.borer.{Decoder, Encoder}

import scala.reflect.ClassTag

trait ErrorType[T] { outer =>
  type E <: Throwable
  def enc: Encoder[E]
  def dec: Decoder[E]
  def classTag: ClassTag[E]
}

object ErrorType {
  implicit def dec[T](implicit et: ErrorType[T]): Decoder[et.E]       = et.dec
  implicit def enc[T](implicit et: ErrorType[T]): Encoder[et.E]       = et.enc
  implicit def classTag[T](implicit et: ErrorType[T]): ClassTag[et.E] = et.classTag

  def bind[T, Err <: Throwable: Encoder: Decoder: ClassTag]: ErrorType[T] = new ErrorType[T] {
    override type E = Err
    override def enc: Encoder[E]       = Encoder[E]
    override def dec: Decoder[E]       = Decoder[E]
    override def classTag: ClassTag[E] = scala.reflect.classTag[E]
  }
}
