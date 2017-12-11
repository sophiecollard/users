package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.fcomb.akka.http.CirceSupport._

import users.domain._
import users.services.UserManagement

import scala.concurrent.{ExecutionContext, Future}

class AdminApi(service: UserManagement[Future[?]])(implicit ec: ExecutionContext) {

  val userIdMatcher = Segment.map(User.Id(_))

  val routes: Route = pathPrefix("users") {
    pathEndOrSingleSlash {
      get {
        val usersF = service.all()
        onSuccess(usersF) {
          case Left(error) => complete(error)
          case Right(users) => complete(users)
        }
      }
    } ~ pathPrefix(userIdMatcher) { id =>
      pathEndOrSingleSlash {
        get {
          val userF = service.get(id)
          onSuccess(userF) {
            case Left(error) => complete(error)
            case Right(user) => complete(user)
          }
        } ~ delete {
          val deletedF = service.delete(id)
          onSuccess(deletedF) {
            case Left(error) => complete(error)
            case Right(_) => complete(StatusCodes.OK)
          }
        }
      } ~ path("reset-password") {
        post {
          val userF = service.resetPassword(id)
          onSuccess(userF) {
            case Left(error) => complete(error)
            case Right(user) => complete(user)
          }
        }
      } ~ path("block") {
        post {
          val userF = service.block(id)
          onSuccess(userF) {
            case Left(error) => complete(error)
            case Right(user) => complete(user)
          }
        }
      } ~ path("unblock") {
        post {
          val userF = service.unblock(id)
          onSuccess(userF) {
            case Left(error) => complete(error)
            case Right(user) => complete(user)
          }
        }
      }
    }
  }

}