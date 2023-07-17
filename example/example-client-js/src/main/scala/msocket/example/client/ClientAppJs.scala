package msocket.example.client

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.stream.scaladsl.Source
import csw.example.api.client.ExampleClient
import msocket.api.Subscription
import msocket.portable.PortablePekko

import scala.concurrent.duration.DurationLong

/** ScalaJS based client application that will send requests to the server */
class ClientAppJs(client: ExampleClient)(implicit actorSystem: ActorSystem[_]) {
//  import actorSystem.executionContext

  def testRun(): Unit = {
//    client.hello("mushtaq").onComplete(x => println(s"==============================> $x"))
//    client.hello("fool").onComplete(x => println(s"==============================> $x"))
//    client.hello("idiot").onComplete(x => println(s"==============================> $x"))

    def stream(x: Int): Subscription = {
//      val numberStream: Source[String, Subscription] = client.helloStream("koo")
      val numberStream: Source[Int, Subscription] = client.getNumbers(x)
      numberStream.onNext(m => println(s"message: $m"))
      numberStream.onError(m => println(s"error: $m"))
      numberStream.onCompleted(() => println(s"completed"))
      numberStream.subscription
    }

//    stream(3)
    val subscription = stream(5)
//    stream(0)
//    stream(-1)

//    client.square(3).onComplete(x => println(s"==============================> $x"))
//    client.square(4).onComplete(x => println(s"==============================> $x"))

    PortablePekko.setTimeout(5.seconds) {
      subscription.cancel()
    }
  }

}
