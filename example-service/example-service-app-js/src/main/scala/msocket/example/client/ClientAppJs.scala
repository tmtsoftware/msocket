package msocket.example.client

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import csw.example.api.client.ExampleClient
import msocket.api.Subscription

class ClientAppJs(client: ExampleClient)(implicit actorSystem: ActorSystem[_]) {
  import actorSystem.executionContext

  def testRun(): Unit = {
    client.hello("mushtaq").onComplete(x => println(s"==============================> $x"))
    client.hello("fool").onComplete(x => println(s"==============================> $x"))
    client.hello("idiot").onComplete(x => println(s"==============================> $x"))

    def stream(x: Int): Unit = {
      val numberStream: Source[Int, Subscription] = client.getNumbers(x)
      numberStream.onMessage(println)
      numberStream.onError(println)
    }

    stream(3)
    stream(0)
    stream(-1)

    client.square(3).onComplete(x => println(s"==============================> $x"))
    client.square(4).onComplete(x => println(s"==============================> $x"))
  }

}
