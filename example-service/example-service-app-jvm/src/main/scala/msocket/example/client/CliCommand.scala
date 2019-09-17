package msocket.example.client

sealed trait CliCommand

object CliCommand {
  case class Login()    extends CliCommand
  case class Logout()   extends CliCommand
  case class MakeCall() extends CliCommand
}
