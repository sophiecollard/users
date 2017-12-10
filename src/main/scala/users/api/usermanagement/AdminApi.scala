package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import users.domain.User
import users.services._

import scala.concurrent.{ExecutionContext, Future}

class AdminApi(service: UserManagement[Future[?]])(implicit ec: ExecutionContext) {

  val userIdMatcher = Segment.map(User.Id(_))

  val routes: Route = pathPrefix("users") {
    pathEndOrSingleSlash {
      get {
        complete(StatusCodes.NotImplemented)
      }
    } ~ pathPrefix(userIdMatcher) { id =>
      pathEndOrSingleSlash {
        get {
          complete(StatusCodes.NotImplemented)
        } ~ delete {
          complete(StatusCodes.NotImplemented)
        }
      } ~ path("reset-password") {
        post {
          complete(StatusCodes.NotImplemented)
        }
      } ~ path("block") {
        post {
          complete(StatusCodes.NotImplemented)
        }
      } ~ path("unblock") {
        post {
          complete(StatusCodes.NotImplemented)
        }
      }
    }
  }

}