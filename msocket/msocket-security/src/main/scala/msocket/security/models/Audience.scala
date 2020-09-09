package msocket.security.models

/**
 * Contains the audience of access token
 */
case class Audience(value: Seq[String] = Seq.empty)

object Audience {
  val empty: Audience = Audience()

  def apply(aud: String): Audience = Audience(Seq(aud))
}
