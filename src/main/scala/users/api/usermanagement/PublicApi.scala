package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import cats.implicits._
import io.fcomb.akka.http.CirceSupport._

import users.api.domain.SignUpInput
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
          val userF = service.signUp(
            input.userName,
            input.emailAddress,
            input.password.pure[Option]
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
            val userF = service.get(id)
            onSuccess(userF) {
              case Left(error) => complete(error)
              case Right(user) => complete(user)
            }
          }
        } ~ path("email") {
          put {
            entity(as[EmailAddress]) { newEmailAddress =>
              val userF = service.updateEmail(id, newEmailAddress)
              onSuccess(userF) {
                case Left(error) => complete(error)
                case Right(user) => complete(user)
              }
            }
          }
        } ~ path("password") {
          put {
            entity(as[Password]) { newPassword =>
              val userF = service.updatePassword(id, newPassword)
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

}