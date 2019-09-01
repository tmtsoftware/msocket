package msocket.example.server

object ServerMain {

  def main(args: Array[String]): Unit = {
    val wiring = new ServerWiring

    wiring.rSocketServer.start("0.0.0.0", 7000)
    wiring.exampleServer.startServer("0.0.0.0", 5000)
  }

}
