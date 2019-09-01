package msocket.example.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import csw.example.api.client.ExampleClient

import scala.concurrent.{ExecutionContext, Future}

class ClientAppJs(client: ExampleClient)(implicit ec: ExecutionContext) {

  def testRun(): Unit = {
    client.hello("xyz").onComplete(println)
    client.hello("abc").onComplete(println)

    client.square(3).onComplete(println)
    client.square(4).onComplete(println)

    val numberStream: Source[Int, Future[Option[String]]] = client.getNumbers(3)
    numberStream.mat.onComplete(println)
    numberStream.onMessage = x => println(s"*******$x")

    client.hello("mushtaq").onComplete(println)

    val postHelloStream: Source[String, NotUsed] = client.helloStream("mushtaq")
    postHelloStream.onMessage = x => println(s"******$x")

    client.hello("msuhtaq1").onComplete(println)
  }

}
