package msocket.simple.server

object ServerApp {

  def main(args: Array[String]): Unit = {
    val wiring = new Wiring
    wiring.simpleServer.startServer("0.0.0.0", 5000)
  }

}
