package msocket.security.codecs

import io.bullet.borer.Dom.{ArrayElem, Element, StringElem}
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec
import io.bullet.borer.{Codec, Decoder, Dom, Encoder}
import msocket.security.models.{Access, AccessToken, Audience, Authorization, Permission}

trait AuthCodecs {
  implicit lazy val accessCodec: Codec[Access]               = deriveCodec
  implicit lazy val authorizationCodec: Codec[Authorization] = deriveCodec
  implicit lazy val permissionCodec: Codec[Permission]       = deriveCodec
  implicit lazy val accessTokenCodec: Codec[AccessToken]     = deriveCodec

  implicit lazy val audienceEnc: Encoder[Audience] = implicitly[Encoder[Element]].contramap[Audience] { audience =>
    audience.value match {
      case head :: Nil => StringElem(head)
      case xs          => ArrayElem.Unsized(xs.map(StringElem.apply)*)
    }

  }

  implicit lazy val audienceDec: Decoder[Audience] = implicitly[Decoder[Element]].map[Audience] {
    case Dom.NullElem                    => Audience()
    case Dom.StringElem(value)           => Audience(value)
    case Dom.ArrayElem.Unsized(elements) => Audience(elements.collect { case StringElem(value) => value })
    case _                               => throw new RuntimeException("parsing failed due to invalid value")
  }
}
