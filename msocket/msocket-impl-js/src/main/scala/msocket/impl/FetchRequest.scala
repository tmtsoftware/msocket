package msocket.impl

import org.scalajs.dom.experimental.{
  AbortSignal,
  BodyInit,
  HeadersInit,
  HttpMethod,
  ReferrerPolicy,
  RequestCache,
  RequestCredentials,
  RequestInit,
  RequestMode,
  RequestRedirect
}

import scala.scalajs.js
import scala.scalajs.js.UndefOr

class FetchRequest extends RequestInit {
  override var method: UndefOr[HttpMethod]              = js.undefined
  override var headers: UndefOr[HeadersInit]            = js.undefined
  override var body: UndefOr[BodyInit]                  = js.undefined
  override var referrer: UndefOr[String]                = js.undefined
  override var referrerPolicy: UndefOr[ReferrerPolicy]  = js.undefined
  override var mode: UndefOr[RequestMode]               = js.undefined
  override var credentials: UndefOr[RequestCredentials] = js.undefined
  override var cache: UndefOr[RequestCache]             = js.undefined
  override var redirect: UndefOr[RequestRedirect]       = js.undefined
  override var integrity: UndefOr[String]               = js.undefined
  override var keepalive: UndefOr[Boolean]              = js.undefined
  override var signal: UndefOr[AbortSignal]             = js.undefined
  override var window: UndefOr[Null]                    = js.undefined
}
