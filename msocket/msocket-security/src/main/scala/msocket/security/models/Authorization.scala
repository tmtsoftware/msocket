package msocket.security.models

/**
 * Contains
 *
 * @param permissions
 */
case class Authorization(permissions: Set[Permission] = Set.empty)

object Authorization {
  val empty: Authorization = Authorization()
}
