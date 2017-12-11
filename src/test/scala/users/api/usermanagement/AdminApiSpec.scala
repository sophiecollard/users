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
import users.utils._

class AdminApiSpec extends Specification
  with Specs2RouteTest
  with AwaitHelper {

  sequential

  val config = ConfigFactory.load().to[ApplicationConfig].get // not safe!

  val apis = Apis.fromApplicationConfig.run(config)

  val api = apis.adminUserManagement

  val user: User =
    await(
      apis
        .services
        .userManagement
        .signUp(
          userName = UserName("test_user"),
          emailAddress = EmailAddress("someone@paidy.com"),
          password = Some(Password("secret_password"))
        )
    ).toOption.get

  "AdminApi" should {
    "get all users" in {
      Get("/users") ~> api.routes ~> check {
        status === StatusCodes.OK
        responseAs[List[User]] must contain(user)
      }
    }

    "get user" in {
      Get(s"/users/${user.id.value}") ~> api.routes ~> check {
        status === StatusCodes.OK
        responseAs[User] === user
      }
    }

    "block user" in {
      Post(s"/users/${user.id.value}/block") ~> api.routes ~> check {
        status === StatusCodes.OK
        val blockedUser = responseAs[User]
        blockedUser.status === User.Status.Blocked
      }
    }

    "unblock user" in {
      Post(s"/users/${user.id.value}/unblock") ~> api.routes ~> check {
        status === StatusCodes.OK
        val activeUser = responseAs[User]
        activeUser.status === User.Status.Active
      }
    }

    "reset user password" in {
      Post(s"/users/${user.id.value}/reset-password") ~> api.routes ~> check {
        status === StatusCodes.OK
        val updatedUser = responseAs[User]
        updatedUser.password must beNone
      }
    }

    "delete user" in {
      // users must be blocked before they can be deleted
      await(apis.services.userManagement.block(user.id)) must beRight
      Delete(s"/users/${user.id.value}") ~> api.routes ~> check {
        status === StatusCodes.OK
      }
      await(apis.services.userManagement.get(user.id)).toOption.get.status === User.Status.Deleted
    }
  }

}