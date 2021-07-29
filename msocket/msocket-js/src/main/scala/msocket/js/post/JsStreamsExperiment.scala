package msocket.js.post

import tmttyped.std.Transformer
import tmttyped.std.global.TransformStream

object JsStreamsExperiment {
  def splitStream(splitOn: String): TransformStream[String, String] = {
    var buffer = ""

    new TransformStream(
      Transformer[String, String]((), ())
        .setTransform { (chunk, controller) =>
          buffer += chunk
          val parts = buffer.split(splitOn)
          parts.init.foreach { part =>
            controller.enqueue(part)
          }
          buffer = parts.lastOption.getOrElse("")
        }
        .setFlush { controller =>
          if (buffer.nonEmpty) controller.enqueue(buffer)
        }
    )
  }
}
