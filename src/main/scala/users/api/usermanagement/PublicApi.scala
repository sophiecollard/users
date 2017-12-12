package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import cats.data.EitherT
import cats.implicits._
import io.fcomb.akka.http.CirceSupport._

import users.api.domain.{HttpError, SignUpInput}
import users.api.domain.AdminApiHttpError._
import users.api.utils.AuthHelper
import users.domain._
import users.services.UserManagement
import users.utils.CirceHelper._

import scala.concurrent.{ExecutionContext, Future}

class PublicApi(service: UserManagement[Future[?]])(implicit ec: ExecutionContext)
  extends AuthHelper {

  val routes: Route = pathPrefix("users") {
    path("sign-up") {
      post {
        entity(as[SignUpInput]) { input =>
          complete(StatusCodes.NotImplemented)
          val userF = signUp(
            input.userName,
            input.emailAddress,
            input.password
          )
          onSuccess(userF) {
            case Left(error) => complete(error)
            case Right(user) => complete(StatusCodes.Created, user)
          }
        }
      }
    } ~ pathPrefix("me") {
      userIdFromAuthHeader { id =>
        pathEndOrSingleSlash {
          get {
            val userF = getUser(id)
            onSuccess(userF) {
              case Left(error) => complete(error)
              case Right(user) => complete(user)
            }
          }
        } ~ path("email") {
          put {
            entity(as[EmailAddress]) { newEmailAddress =>
              val userF = updateUserEmail(id, newEmailAddress)
              onSuccess(userF) {
                case Left(error) => complete(error)
                case Right(user) => complete(user)
              }
            }
          }
        } ~ path("password") {
          put {
            entity(as[Password]) { newPassword =>
              val userF = updateUserPassword(id, newPassword)
              onSuccess(userF) {
                case Left(error) => complete(error)
                case Right(user) => complete(user)
              }
            }
          }
        }
      }
    }
  }

  def signUp(userName: UserName, emailAddress: EmailAddress, password: Password): Future[HttpError Either User] =
    EitherT(service.signUp(userName, emailAddress, password.pure[Option]))
      .leftMap(_.asHttpError)
      .value

  def getUser(id: User.Id): Future[HttpError Either User] =
    EitherT(service.get(id))
      .leftMap(_.asHttpError)
      .value

  def updateUserEmail(id: User.Id, newEmailAddress: EmailAddress): Future[HttpError Either User] =
    EitherT(service.updateEmail(id, newEmailAddress))
      .leftMap(_.asHttpError)
      .value

  def updateUserPassword(id: User.Id, newPassword: Password): Future[HttpError Either User] =
    EitherT(service.updatePassword(id, newPassword))
      .leftMap(_.asHttpError)
      .value

}