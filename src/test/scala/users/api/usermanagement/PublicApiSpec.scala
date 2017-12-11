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
import users.utils.AwaitHelper
import users.utils.CirceHelper._

class PublicApiSpec extends Specification
  with Specs2RouteTest
  with AwaitHelper {

  sequential

  val config = ConfigFactory.load().to[ApplicationConfig].get // not safe!

  val apis = Apis.fromApplicationConfig.run(config)

  val api = apis.publicUserManagement

  var user: User = _
  def authHeader = Authorization(OAuth2BearerToken(token = user.id.value))

  "PublicApi" should {
    "sign new user up" in {
      val input = SignUpInput(
        userName = UserName("Martin"),
        emailAddress = EmailAddress("m.odersky@paidy.com"),
        password = Password("change_me_please")
      )
      Post("/users/sign-up", input) ~> api.routes ~> check {
        status === StatusCodes.Created
        user = responseAs[User]
        ok
      }
    }

    "get user's details" in {
      Get(s"/users/me").addHeader(authHeader) ~> api.routes ~> check {
        status === StatusCodes.OK
        responseAs[User] === user
      }
    }

    "update user's email address" in {
      val newEmailAddress = EmailAddress("m.odersky@epfl.ch")
      Put(s"/users/me/email", newEmailAddress).addHeader(authHeader) ~> api.routes ~> check {
        status === StatusCodes.OK
        responseAs[User].emailAddress === newEmailAddress
      }
    }

    "update user's password" in {
      val newPassword = Password("k4k(YM&CzQGs[")
      Put(s"/users/me/password", newPassword).addHeader(authHeader) ~> api.routes ~> check {
        status === StatusCodes.OK
        responseAs[User].password must beSome(newPassword)
      }
    }
  }

}