package msocket.example.client

import org.apache.pekko.actor.typed.ActorSystem
import csw.example.api.client.ExampleClient

import scala.concurrent.ExecutionContext

/** Client application that will send requests to the server */
class ClientApp(client: ExampleClient)(implicit ec: ExecutionContext, actorSystem: ActorSystem[?]) {

  def testRun(): Unit = {
//    client.getNumbers(3).take(5).runForeach(println)
//    Thread.sleep(Int.MaxValue)
//    client.helloStream("mushtaq").runForeach(println)
//    Thread.sleep(Int.MaxValue)
    client.hello("mushtaq").onComplete(x => println(s"==============================> $x"))
    client.hello("fool").onComplete(x => println(s"==============================> $x"))
    client.hello("idiot").onComplete(x => println(s"==============================> $x"))

    client.getNumbers(3).runForeach(println).onComplete(println)
    client.getNumbers(0).runForeach(println).onComplete(println)
    client.getNumbers(-1).runForeach(println).onComplete(println)

    client.square(3).onComplete(x => println(s"==============================> $x"))
    client.square(4).onComplete(x => println(s"==============================> $x"))
    client.randomBag().onComplete(x => println(s"==============================> $x"))
    client.randomBagStream().runForeach(x => println(s"==============================> $x")).onComplete(println)
  }

}
