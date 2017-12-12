package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.testkit.Specs2RouteTest
import com.typesafe.config.ConfigFactory
import io.fcomb.akka.http.CirceSupport._
import org.specs2.mutable.Specification
import pureconfig.syntax._

import users.api.domain.SignUpInput
import users.config.ApplicationConfig
import users.domain._
import users.main.Apis
import users.services.UserManagement
import users.utils.AwaitHelper
import users.utils.CirceHelper._

import scala.concurrent.Future

class PublicApiSpec extends Specification
  with Specs2RouteTest
  with AwaitHelper {

  sequential

  val config = ConfigFactory.load().to[ApplicationConfig].get // not safe!

  val apis = Apis.fromApplicationConfig.run(config)

  val api = apis.publicUserManagement

  implicit val service = apis.services.userManagement

  import PublicApiSpec._

  var mainTestUser: User = _
  def mainTestUserAuth = Authorization(OAuth2BearerToken(token = mainTestUser.id.value))

  val unknownUserAuth = Authorization(OAuth2BearerToken(token = "unknown_user"))

  val deletedUser = createUser(
    userName = UserName("deleted_user_2"),
    emailAddress = EmailAddress("deleted@paidy.com"),
    password = Some(Password("secret_password"))
  )
  await(apis.services.userManagement.block(deletedUser.id)) must beRight
  await(apis.services.userManagement.delete(deletedUser.id)) must beRight
  val deletedUserAuth = Authorization(OAuth2BearerToken(token = deletedUser.id.value))

  "PublicApi" should {
    "sign new user up" in {
      val input = SignUpInput(
        userName = UserName("Martin"),
        emailAddress = EmailAddress("m.odersky@paidy.com"),
        password = Password("change_me_please")
      )

      "ok" in {
        Post("/users/sign-up", input) ~> api.routes ~> check {
          status === StatusCodes.Created
          mainTestUser = responseAs[User]
          ok
        }
      }

      "conflict (exisiting user name)" in {
        Post("/users/sign-up", input) ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }
    }

    "get user's details" in {
      "ok" in {
        Get(s"/users/me").addHeader(mainTestUserAuth) ~> api.routes ~> check {
          status === StatusCodes.OK
          responseAs[User] === mainTestUser
        }
      }

      "not found" in {
        Get(s"/users/me").addHeader(unknownUserAuth) ~> api.routes ~> check {
          status === StatusCodes.NotFound
        }
      }
    }

    "update user's email address" in {
      val newEmailAddress = EmailAddress("m.odersky@epfl.ch")

      "ok" in {
        Put(s"/users/me/email", newEmailAddress).addHeader(mainTestUserAuth) ~> api.routes ~> check {
          status === StatusCodes.OK
          responseAs[User].emailAddress === newEmailAddress
        }
      }

      "not found" in {
        Put(s"/users/me/email", newEmailAddress).addHeader(unknownUserAuth) ~> api.routes ~> check {
          status === StatusCodes.NotFound
        }
      }

      "conflict (deleted user)" in {
        Put(s"/users/me/email", newEmailAddress).addHeader(deletedUserAuth) ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }
    }

    "update user's password" in {
      val newPassword = Password("k4k(YM&CzQGs[")

      "ok" in {
        Put(s"/users/me/password", newPassword).addHeader(mainTestUserAuth) ~> api.routes ~> check {
          status === StatusCodes.OK
          responseAs[User].password must beSome(newPassword)
        }
      }

      "not found" in {
        Put(s"/users/me/password", newPassword).addHeader(unknownUserAuth) ~> api.routes ~> check {
          status === StatusCodes.NotFound
        }
      }

      "conflict (deleted user)" in {
        Put(s"/users/me/password", newPassword).addHeader(deletedUserAuth) ~> api.routes ~> check {
          status === StatusCodes.Conflict
        }
      }
    }
  }

}

object PublicApiSpec extends AwaitHelper {

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