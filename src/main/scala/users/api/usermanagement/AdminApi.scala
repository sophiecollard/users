package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.data.EitherT
import cats.implicits._
import io.fcomb.akka.http.CirceSupport._

import users.api.domain.HttpError
import users.api.domain.AdminApiHttpError._
import users.domain._
import users.services.UserManagement

import scala.concurrent.{ExecutionContext, Future}

class AdminApi(service: UserManagement[Future[?]])(implicit ec: ExecutionContext) {

  val userIdMatcher = Segment.map(User.Id(_))

  val routes: Route = pathPrefix("users") {
    pathEndOrSingleSlash {
      get {
        val usersF = getAllUsers()
        onSuccess(usersF) {
          case Left(error) => complete(error)
          case Right(users) => complete(users)
        }
      }
    } ~ pathPrefix(userIdMatcher) { id =>
      pathEndOrSingleSlash {
        get {
          val userF = getUser(id)
          onSuccess(userF) {
            case Left(error) => complete(error)
            case Right(user) => complete(user)
          }
        } ~ delete {
          val deletedF = deleteUser(id)
          onSuccess(deletedF) {
            case Left(error) => complete(error)
            case Right(_) => complete(StatusCodes.OK)
          }
        }
      } ~ path("reset-password") {
        post {
          val userF = resetUserPassword(id)
          onSuccess(userF) {
            case Left(error) => complete(error)
            case Right(user) => complete(user)
          }
        }
      } ~ path("block") {
        post {
          val userF = blockUser(id)
          onSuccess(userF) {
            case Left(error) => complete(error)
            case Right(user) => complete(user)
          }
        }
      } ~ path("unblock") {
        post {
          val userF = unblockUser(id)
          onSuccess(userF) {
            case Left(error) => complete(error)
            case Right(user) => complete(user)
          }
        }
      }
    }
  }

  def getAllUsers(): Future[HttpError Either List[User]] =
    EitherT(service.all())
      .leftMap(_.asHttpError)
      .value

  def getUser(id: User.Id): Future[HttpError Either User] =
    EitherT(service.get(id))
      .leftMap(_.asHttpError)
      .value

  def deleteUser(id: User.Id): Future[HttpError Either Done] =
    EitherT(service.delete(id))
      .leftMap(_.asHttpError)
      .value

  def resetUserPassword(id: User.Id): Future[HttpError Either User] =
    EitherT(service.resetPassword(id))
      .leftMap(_.asHttpError)
      .value

  def blockUser(id: User.Id): Future[HttpError Either User] =
    EitherT(service.block(id))
      .leftMap(_.asHttpError)
      .value

  def unblockUser(id: User.Id): Future[HttpError Either User] =
    EitherT(service.unblock(id))
      .leftMap(_.asHttpError)
      .value

}