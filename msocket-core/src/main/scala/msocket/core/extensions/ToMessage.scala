//package msocket.core.extensions
//
//import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
//import akka.util.ByteString
//import io.bullet.borer.compat.akka._
//import io.bullet.borer.{Encoder, Target}
//import msocket.core.api.Encoding
//
//trait MessageCodec[T] {
//  def toMessage(input: T): Message
//  def fromMessage(message: Message): T
//}
//
//object ToMessage {
//
//  implicit def singleTextMessage[T: Encoder](implicit target: Target, encoding: Encoding): MessageCodec[T] = new MessageCodec[T] {
//    override def toMessage(input: T): Message = ???
//
//    override def fromMessage(message: Message): T = ???
//  }
//
//  implicit class ValueToMessage[T: Encoder](x: T)(implicit target: Target) {
//    def byteString: ByteString = target.encode(x).to[ByteString].result
//    def text: String           = byteString.utf8String
//
//    def textMessage: TextMessage.Strict     = TextMessage.Strict(text)
//    def binaryMessage: BinaryMessage.Strict = BinaryMessage.Strict(byteString)
//  }
//}
