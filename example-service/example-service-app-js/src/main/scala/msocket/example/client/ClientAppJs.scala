package msocket.example.client

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import csw.example.api.client.ExampleClient
import msocket.api.models.{StreamStatus, Subscription}

import scala.concurrent.{ExecutionContext, Future}

class ClientAppJs(client: ExampleClient)(implicit ec: ExecutionContext, mat: Materializer) {

  def testRun(): Unit = {
    client.hello("xyz").onComplete(println)
    client.hello("abc").onComplete(println)

    client.square(3).onComplete(println)
    client.square(4).onComplete(println)

    val numberStream: Source[Int, Future[StreamStatus]] = client.getNumbers(3)
    numberStream.materializedValue.onComplete(println)
    numberStream.foreach { x =>
      println(s"**********************  $x")
    }

    client.hello("mushtaq").onComplete(println)
    val postHelloStream: Source[String, Subscription] = client.helloStream("mushtaq")
    import msocket.impl.extensions.SourceExtensions.SourceWithSubscribe
    postHelloStream.subscribe { x =>
      println(s"--------> $x")
    }

    client.hello("msuhtaq1").onComplete(println)
  }

}
