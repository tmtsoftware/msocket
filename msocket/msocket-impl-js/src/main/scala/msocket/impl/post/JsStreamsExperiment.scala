package msocket.impl.post

import typings.std.Transformer
import typings.std.global.{JSON, TransformStream}

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

  def parseJson(): TransformStream[String, FetchEventJs] = {
    new TransformStream(
      Transformer[String, FetchEventJs]()
        .setTransform { (chunk, controller) =>
          controller.enqueue(FetchEventJs(JSON.parse(chunk)))
        }
    )
  }
}
