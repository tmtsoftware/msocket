package msocket.example.client

import akka.actor.typed.ActorSystem
import csw.example.api.client.ExampleClient

import scala.concurrent.ExecutionContext

class ClientApp(client: ExampleClient)(implicit ec: ExecutionContext, actorSystem: ActorSystem[_]) {

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

    //    client.hello("msuhtaq1").onComplete(x => println(s"==============================> $x"))
//    client.square(3).onComplete(x => println(s"==============================> $x"))
//    client.square(4).onComplete(x => println(s"==============================> $x"))
  }

}
