package msocket.impl.post

import typings.std.^.JSON
import typings.std.{TransformStream, Transformer}

import scala.scalajs.js

object JsStreamsExperiment {
  def splitStream(splitOn: String): TransformStream[String, String] = {
    var buffer = ""

    val transformer = Transformer[String, String](
      transform = (chunk, controller) => {
        buffer += chunk
        val parts = buffer.split(splitOn)
        parts.slice(0, -1).foreach(part => controller.enqueue(part))
        buffer = parts(parts.length - 1)
      },
      flush = controller => {
        if (buffer.nonEmpty) controller.enqueue(buffer)
      }
    )
    cast(transformer)
  }

  def parseJson(): TransformStream[String, FetchEventJs] = {
    val transformer = Transformer[String, FetchEventJs](
      transform = (chunk, controller) => {
        controller.enqueue(FetchEventJs(JSON.parse(chunk)))
      }
    )
    cast(transformer)
  }

  def cast[I, O](t: Transformer[I, O]): TransformStream[I, O] =
    TransformStream
      .newInstance1(t.asInstanceOf[Transformer[js.Object, js.Object]])
      .asInstanceOf[TransformStream[I, O]]
}
