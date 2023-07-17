package org.apache.pekko

sealed abstract class Done extends Serializable
case object Done           extends Done
