package msocket.security.models

/**
 * Contains roles of a user or client
 */
case class Access(roles: Set[String] = Set.empty)

object Access {
  val empty: Access = Access()
}
