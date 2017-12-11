package users.api.utils

import akka.http.scaladsl.model.headers.{Authorization, HttpChallenges, OAuth2BearerToken}
import akka.http.scaladsl.server.{AuthenticationFailedRejection, Directive1}
import akka.http.scaladsl.server.Directives.{optionalHeaderValueByType, provide, reject}

import users.domain.User

trait AuthHelper {

  def userIdFromAuthHeader: Directive1[User.Id] =
    optionalHeaderValueByType(classOf[Authorization]).flatMap {
      case Some(auth) => userIdDirective(userIdFromAuth(auth))
      case None => credentialsMissing: Directive1[User.Id]
    }

  def userIdFromAuth(auth: Authorization): Option[User.Id] =
    auth.credentials match {
      case OAuth2BearerToken(token) =>
        Some(User.Id(token))
      case _ =>
        None
    }

  def userIdDirective(maybeUserId: Option[User.Id]): Directive1[User.Id] =
    maybeUserId
      .map(provide)
      .getOrElse(credentialsRejected: Directive1[User.Id])

  val httpChallenge = HttpChallenges.oAuth2(realm = "user management")

  val credentialsMissing =
    reject(
      AuthenticationFailedRejection(
        AuthenticationFailedRejection.CredentialsMissing,
        httpChallenge
      )
    )

  val credentialsRejected =
    reject(
      AuthenticationFailedRejection(
        AuthenticationFailedRejection.CredentialsRejected,
        httpChallenge
      )
    )

}