package msocket.impl.post

import akka.Done
import io.bullet.borer.{Decoder, Encoder}
import msocket.api.ContentEncoding.JsonText
import msocket.api.ContentType.Json
import msocket.api.models.ErrorType
import msocket.api.{ErrorProtocol, Subscription}
import msocket.portable.Observer
import org.scalajs.dom.experimental.WriteableStream
import typings.std.global.{JSON, TextDecoderStream, TransformStream, WritableStream}
import typings.std.{Transformer, UnderlyingSink}

import scala.concurrent.{ExecutionContext, Promise}
import scala.util.control.NonFatal

object JsStreamsExperiment {
  def splitStream(splitOn: String): TransformStream[String, String] = {
    var buffer = ""

    new TransformStream(
      Transformer[String, String]()
        .setTransform { (chunk, controller) =>
          buffer += chunk
          val parts = buffer.split(splitOn)
          parts.slice(0, -1).foreach(part => controller.enqueue(part))
          buffer = parts(parts.length - 1)
        }
        .setFlush { controller =>
          if (buffer.nonEmpty) controller.enqueue(buffer)
        }
    )
  }

  def parseJson(promise: Promise[Done])(implicit ec: ExecutionContext): TransformStream[String, FetchEventJs] = {
    new TransformStream(
      Transformer[String, FetchEventJs]()
        .setTransform { (chunk, controller) =>
          controller.enqueue(FetchEventJs(JSON.parse(chunk)))
          promise.future.foreach(_ => controller.terminate())
        }
    )
  }

  def sinkOf[Req: Encoder: ErrorProtocol, Res: Decoder: Encoder](observer: Observer[Res]): WriteableStream[FetchEventJs] = {
    new WritableStream[FetchEventJs](
      UnderlyingSink[FetchEventJs]()
        .setWrite { (fetchEventJs, controller) =>
          val jsonString = fetchEventJs.data
          if (jsonString != "") {
            try {
              val maybeErrorType = fetchEventJs.errorType.toOption.map(ErrorType.from)
              observer.onNext(JsonText.decodeFull(jsonString, maybeErrorType))
            } catch {
              case NonFatal(ex) =>
                observer.onError(ex)
                controller.error(ex.getMessage)
            }
          }
        }
    ).asInstanceOf[WriteableStream[FetchEventJs]]
  }

  def requestStream[Req: Encoder: ErrorProtocol, Res: Decoder: Encoder](
      uri: String,
      request: Req,
      observer: Observer[Res]
  )(implicit ec: ExecutionContext): Subscription = {
    val promise = Promise[Done]()
    FetchHelper.postRequest[Req](uri, request, Json).map { response =>
      response.body
        .pipeThrough[String](new TextDecoderStream())
        .pipeThrough[String](JsStreamsExperiment.splitStream("\n"))
        .pipeThrough[FetchEventJs](JsStreamsExperiment.parseJson(promise))
        .pipeTo(JsStreamsExperiment.sinkOf[Req, Res](observer))
    }
    () => promise.trySuccess(Done): Unit
  }

}
