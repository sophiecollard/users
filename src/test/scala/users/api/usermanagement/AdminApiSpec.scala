package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.Specs2RouteTest
import com.typesafe.config.ConfigFactory
import io.fcomb.akka.http.CirceSupport._
import org.specs2.mutable.Specification
import pureconfig.syntax._

import users.config.ApplicationConfig
import users.domain._
import users.main.Apis
import users.services.UserManagement
import users.utils._

import scala.concurrent.Future

class AdminApiSpec extends Specification
  with Specs2RouteTest
  with AwaitHelper {

  sequential

  val config = ConfigFactory.load().to[ApplicationConfig].get // not safe!

  val apis = Apis.fromApplicationConfig.run(config)

  val api = apis.adminUserManagement

  implicit val service = apis.services.userManagement

  import AdminApiSpec._

  val mainTestUser = createUser(
    userName = UserName("main_test_user"),
    emailAddress = EmailAddress("test@paidy.com"),
    password = Some(Password("secret_password"))
  )

  val activeUser = createUser(
    userName = UserName("active_user"),
    emailAddress = EmailAddress("active@paidy.com"),
    password = Some(Password("secret_password"))
  )

  val blockedUser = createUser(
    userName = UserName("blocked_user"),
    emailAddress = EmailAddress("blocked@paidy.com"),
    password = Some(Password("secret_password"))
  )
  await(apis.services.userManagement.block(blockedUser.id)) must beRight

  val deletedUser = createUser(
    userName = UserName("deleted_user"),
    emailAddress = EmailAddress("deleted@paidy.com"),
    password = Some(Password("secret_password"))
  )
  await(apis.services.userManagement.block(deletedUser.id)) must beRight
  await(apis.services.userManagement.delete(deletedUser.id)) must beRight

  "AdminApi" should {
    "get all users" in {
      Get("/users") ~> api.routes ~> check {
        status === StatusCodes.OK
        responseAs[List[User]] must contain(mainTestUser)
      }
    }

    "get user" in {
      "ok" in {
        Get(s"/users/${mainTestUser.id.value}") ~> api.routes ~> check {
          status === StatusCodes.OK
          responseAs[User] === mainTestUser
        }
      }

      "not found" in {
        Get(s"/users/nonexistentuser") ~> api.routes ~> check {
          status === StatusCodes.NotFound
        }
      }
    }

    "block user" in {
      "ok" in {
        Post(s"/users/${mainTestUser.id.value}/block") ~> api.routes ~> check {
          status === StatusCodes.OK
          val blockedUser = responseAs[User]
          blockedUser.status === User.Status.Blocked
        }
      }

      "not found" in {
        Post(s"/users/nonexistentuser/block") ~> api.routes ~> check {
          status === StatusCodes.NotFound
        }
      }

      "conflict (blocked user)" in {
        Post(s"/users/${blockedUser.id.value}/block") ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }

      "conflict (deleted user)" in {
        Post(s"/users/${deletedUser.id.value}/block") ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }
    }

    "unblock user" in {
      "ok" in {
        Post(s"/users/${mainTestUser.id.value}/unblock") ~> api.routes ~> check {
          status === StatusCodes.OK
          val activeUser = responseAs[User]
          activeUser.status === User.Status.Active
        }
      }

      "not found" in {
        Post(s"/users/nonexistentuser/unblock") ~> api.routes ~> check {
          status === StatusCodes.NotFound
        }
      }

      "conflict (active user)" in {
        Post(s"/users/${activeUser.id.value}/unblock") ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }

      "conflict (deleted user)" in {
        Post(s"/users/${deletedUser.id.value}/unblock") ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }
    }

    "reset user password" in {
      "ok" in {
        Post(s"/users/${mainTestUser.id.value}/reset-password") ~> api.routes ~> check {
          status === StatusCodes.OK
          val updatedUser = responseAs[User]
          updatedUser.password must beNone
        }
      }

      "not found" in {
        Post(s"/users/nonexistentuser/reset-password") ~> api.routes ~> check {
          status === StatusCodes.NotFound
        }
      }

      "conflict (deleted user)" in {
        Post(s"/users/${deletedUser.id.value}/reset-password") ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }
    }

    "delete user" in {
      "ok" in {
        // users must be blocked before they can be deleted
        await(apis.services.userManagement.block(mainTestUser.id)) must beRight
        Delete(s"/users/${mainTestUser.id.value}") ~> api.routes ~> check {
          status === StatusCodes.OK
        }
        await(apis.services.userManagement.get(mainTestUser.id)).toOption.get.status === User.Status.Deleted
      }

      "not found" in {
        Delete(s"/users/nonexistentuser") ~> api.routes ~> check {
          status === StatusCodes.NotFound
        }
      }

      "conflict (active user)" in {
        Delete(s"/users/${activeUser.id.value}") ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }

      "conflict (deleted user)" in {
        Delete(s"/users/${deletedUser.id.value}") ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }
    }
  }

}

object AdminApiSpec extends AwaitHelper {

  def createUser(
      userName: UserName,
      emailAddress: EmailAddress,
      password: Option[Password]
  )(implicit service: UserManagement[Future[?]]): User =
    await(
      service.signUp(
        userName,
        emailAddress,
        password
      )
    ).toOption.get

}