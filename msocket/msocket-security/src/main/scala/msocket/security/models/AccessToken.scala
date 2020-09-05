package msocket.security.models

import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

case class AccessToken(realm_access: Access = Access.empty) {
  def hasRealmRole(role: String): Boolean = realm_access.roles.map(_.toLowerCase).contains(role.toLowerCase)
}

object AccessToken {
  implicit lazy val accessTokenCodec: Codec[AccessToken] = deriveCodec
}

case class Access(roles: Set[String] = Set.empty)

object Access {
  val empty: Access                       = Access()
  implicit lazy val access: Codec[Access] = deriveCodec
}
