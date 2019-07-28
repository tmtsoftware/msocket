//package msocket.core.client
//
//import java.util.UUID
//
//import akka.NotUsed
//import akka.actor.ActorSystem
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
//import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink}
//import akka.stream.{ActorMaterializer, Materializer}
//import io.bullet.borer.{Encoder, Target}
//import msocket.core.api.MSocket
//
//import scala.concurrent.Future
//
//class ClientHandler(webSocketRequest: WebSocketRequest)(implicit actorSystem: ActorSystem, target: Target) {
//  implicit val mat: Materializer = ActorMaterializer()
//
//  val (upstreamSink, upstreamSource) =
//    MergeHub.source[Message](perProducerBufferSize = 16).toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both).run()
//
//  val (downstremSink, downstreamSource) =
//    MergeHub.source[Message](perProducerBufferSize = 16).toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both).run()
//
//  val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(downstremSink, upstreamSource)
//
//  val (upgradeResponse, closed) = Http().singleWebSocketRequest(WebSocketRequest("ws://localhost:5000/websocket"), flow)
//
//  def textSocket[RR: Encoder, RS: Encoder](): MSocket[RR, RS] = new TextSocket[RR, RS] {
//    override def requestResponse(message: RR, id: UUID): Future[TextMessage.Strict] = {
////      Source.single(Payload(message, id).textMessage).runWith(upstreamSink)
//      downstreamSource.collectType[TextMessage.Strict].runWith(Sink.head)
//      ???
//    }
//
//    override def requestStream(message: RS, id: UUID): TextMessage.Streamed = {
////      Source.single(Payload(message, id).textMessage).runWith(upstreamSink)
//      downstreamSource.collectType[TextMessage.Streamed]
//      ???
//    }
//  }
//}
