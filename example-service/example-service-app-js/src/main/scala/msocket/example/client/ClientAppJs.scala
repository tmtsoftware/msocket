package msocket.example.client

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source
import csw.example.api.client.ExampleClient
import msocket.api.Subscription
import portable.akka.extensions.PortableAkka._

class ClientAppJs(client: ExampleClient)(implicit actorSystem: ActorSystem[_]) {
  import actorSystem.executionContext

  def testRun(): Unit = {
    client.hello("xyz").onComplete(println)
    client.hello("abc").onComplete(println)

    client.square(3).onComplete(println)
    client.square(4).onComplete(println)

    val numberStream: Source[Int, Subscription] = client.getNumbers(3)
    numberStream.foreach { x =>
      println(s"**********************  $x")
    }

    client.hello("mushtaq").onComplete(println)
    val postHelloStream: Source[String, Subscription] = client.helloStream("mushtaq")
    postHelloStream.subscribe { x =>
      println(s"--------> $x")
    }

    client.hello("msuhtaq1").onComplete(println)
  }

}
