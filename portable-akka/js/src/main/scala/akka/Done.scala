package akka

sealed abstract class Done extends Serializable
case object Done           extends Done
