package msocket.example.client

import akka.stream.Materializer
import csw.example.api.client.ExampleClient

import scala.concurrent.ExecutionContext

class ClientApp(client: ExampleClient)(implicit ec: ExecutionContext, mat: Materializer) {

  def testRun(): Unit = {
    client.getNumbers(3).take(5).runForeach(println)
    Thread.sleep(Int.MaxValue)
    client.helloStream("mushtaq").runForeach(println)
    Thread.sleep(Int.MaxValue)
    client.getNumbers(0).mapMaterializedValue(_.onComplete(println)).runForeach(println)
    client.hello("msuhtaq").onComplete(println)
    client.hello("msuhtaq1").onComplete(println)
    client.square(3).onComplete(println)
    client.square(4).onComplete(println)
  }

}
