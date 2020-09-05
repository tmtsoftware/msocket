package msocket.impl.post

import akka.Done
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType.Json
import msocket.api.models.ErrorType
import msocket.api.{ContentType, ErrorProtocol, Subscription}
import msocket.impl.JsTransport
import msocket.impl.post.HttpJsExtensions.HttpJsEncoding
import msocket.portable.Observer
import org.scalajs.dom.experimental.WriteableStream
import typings.std.global.{JSON, TextDecoderStream, TransformStream, WritableStream}
import typings.std.{Transformer, UnderlyingSink}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

class HttpPostTransportJs[Req: Encoder: ErrorProtocol](uri: String, contentType: ContentType)(implicit ec: ExecutionContext)
    extends JsTransport[Req] {

  override def requestResponse[Res: Decoder: Encoder](req: Req): Future[Res] =
    FetchHelper.postRequest(uri, req, contentType).flatMap(response => contentType.response(response))

  override def requestStream[Res: Decoder: Encoder](request: Req, observer: Observer[Res]): Subscription = {
    val promise = Promise[Done]()
    FetchHelper.postRequest[Req](uri, request, Json).map { response =>
      response.body
        .pipeThrough[String](new TextDecoderStream())
        .pipeThrough[FetchEventJs](parseJson(promise))
        .pipeTo(sinkOf(observer))
    }
    () => {
      promise.trySuccess(Done)
      observer.onCompleted()
    }
  }

  def parseJson[Res: Decoder: Encoder](promise: Promise[Done]): TransformStream[String, FetchEventJs] = {
    new TransformStream(
      Transformer[String, FetchEventJs]()
        .setTransform { (chunk, controller) =>
          if (chunk.nonEmpty && chunk != "\n") {
            val fetchEventJs = FetchEventJs(JSON.parse(chunk))
            controller.enqueue(fetchEventJs)
            promise.future.foreach(_ => controller.terminate())
          }
        }
    )
  }

  def sinkOf[Res: Decoder: Encoder](observer: Observer[Res]): WriteableStream[FetchEventJs] = {
    new WritableStream[FetchEventJs](
      UnderlyingSink[FetchEventJs]()
        .setWrite { (fetchEventJs, controller) =>
          val jsonString = fetchEventJs.data
          try {
            val maybeErrorType = fetchEventJs.errorType.toOption.map(ErrorType.from)
            observer.onNext(JsonText.decodeFull(jsonString, maybeErrorType))
          } catch {
            case NonFatal(ex) =>
              observer.onError(ex)
              controller.error()
          }
        }
    ).asInstanceOf[WriteableStream[FetchEventJs]]
  }

}
