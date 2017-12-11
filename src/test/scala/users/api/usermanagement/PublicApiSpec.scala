package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
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

  "PublicApi" should {
    "sign new user up" in {
      val input = SignUpInput(
        userName = UserName("Martin"),
        emailAddress = EmailAddress("m.odersky@paidy.com"),
        password = Password("change_me_please")
      )
      Post("/users/sign-up", input) ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }

    "get user's details" in {
      Get(s"/users/me") ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }

    "update user's email address" in {
      val newEmailAddress = EmailAddress("m.odersky@epfl.ch")
      Put(s"/users/me/email", newEmailAddress) ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }

    "update user's password" in {
      val newPassword = Password("k4k(YM&CzQGs[")
      Put(s"/users/me/password", newPassword) ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }
  }

}