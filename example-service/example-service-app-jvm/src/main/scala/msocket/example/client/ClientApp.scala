package msocket.example.client

import akka.stream.Materializer
import csw.example.api.client.ExampleClient

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationLong

class ClientApp(client: ExampleClient)(implicit ec: ExecutionContext, mat: Materializer) {

  def testRun(): Unit = {
    client.getNumbers(3).mapMaterializedValue(_.onComplete(println)).runForeach(println)
    client.getNumbers(0).mapMaterializedValue(_.onComplete(println)).runForeach(println)
    client.hello("msuhtaq").onComplete(println)
    client.helloStream("mushtaq").throttle(1, 1.second).runForeach(println)
    client.hello("msuhtaq1").onComplete(println)
    client.square(3).onComplete(println)
    client.square(4).onComplete(println)
  }

}
