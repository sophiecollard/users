package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
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
        }
      }
    } ~ pathPrefix("me") {
      userIdFromAuthHeader { id =>
        pathEndOrSingleSlash {
          get {
            complete(StatusCodes.NotImplemented)
          }
        } ~ path("email") {
          put {
            entity(as[EmailAddress]) { newEmailAddress =>
              complete(StatusCodes.NotImplemented)
            }
          }
        } ~ path("password") {
          put {
            entity(as[Password]) { newPassword =>
              complete(StatusCodes.NotImplemented)
            }
          }
        }
      }
    }
  }

}