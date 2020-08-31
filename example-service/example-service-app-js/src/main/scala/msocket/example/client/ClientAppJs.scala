package msocket.example.client

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import csw.example.api.client.ExampleClient
import msocket.api.Subscription
import msocket.portable.PortableAkka

import scala.concurrent.duration.DurationLong

/** ScalaJS based client application that will send requests to the server */
class ClientAppJs(client: ExampleClient)(implicit actorSystem: ActorSystem[_]) {
  import actorSystem.executionContext

  def testRun(): Unit = {
    client.hello("mushtaq").onComplete(x => println(s"==============================> $x"))
    client.hello("fool").onComplete(x => println(s"==============================> $x"))
    client.hello("idiot").onComplete(x => println(s"==============================> $x"))

    def stream(x: Int): Subscription = {
      val numberStream: Source[Int, Subscription] = client.getNumbers(x)
      numberStream.onNext(println)
      numberStream.onError(println)
      numberStream.subscription
    }

    stream(3)
    val subscription = stream(5)
    stream(0)
    stream(-1)

    client.square(3).onComplete(x => println(s"==============================> $x"))
    client.square(4).onComplete(x => println(s"==============================> $x"))

    PortableAkka.setTimeout(5.seconds) {
      subscription.cancel()
    }
  }

}
