package msocket.example.server

object ServerApp {

  def main(args: Array[String]): Unit = {
    val wiring = new Wiring
    wiring.exampleServer.startServer("0.0.0.0", 5000)
  }

}
