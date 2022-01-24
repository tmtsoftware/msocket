package msocket.security.api

import msocket.security.models.AccessToken

import scala.concurrent.{ExecutionContext, Future}

/**
 * An authorization policy is a way to filter incoming HTTP requests based on rules
 */
trait AuthorizationPolicy {

  /**
   * Implement this method to create an asynchronous AuthorizationPolicy
   */
  def authorize(accessToken: AccessToken): Future[Boolean]
}

object AuthorizationPolicy {

  implicit class AuthorizationPolicyOps(private val target: AuthorizationPolicy) extends AnyVal {

    /**
     * Applies a new authorization policy in combination with previous policy.
     * Passing of both policies is requried for authorization to succeed.
     *
     * @param other new Authorization policy
     * @return combined authorization policy
     */
    def &(other: AuthorizationPolicy)(implicit ec: ExecutionContext): AuthorizationPolicy = { accessToken =>
      val leftF  = target.authorize(accessToken)
      val rightF = other.authorize(accessToken)
      leftF.zipWith(rightF)(_ && _)
    }

    /**
     * Applies a new authorization policy if the previous policy fails.
     *
     * Authorization will succeed if any of the provided policy passes.
     *
     * @param other new Authorization policy
     * @return combined authorization policy
     */
    def |(other: AuthorizationPolicy)(implicit ec: ExecutionContext): AuthorizationPolicy = { accessToken =>
      val leftF  = target.authorize(accessToken)
      val rightF = other.authorize(accessToken)
      leftF.zipWith(rightF)(_ || _)
    }

  }
}
