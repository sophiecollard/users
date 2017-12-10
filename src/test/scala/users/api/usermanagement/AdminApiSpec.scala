package users.api.usermanagement

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.Specs2RouteTest
import org.specs2.mutable.Specification

class AdminApiSpec extends Specification with Specs2RouteTest {

  val api = new AdminApi()

  "AdminApi" should {

    "get all users" in {
      Get("/users") ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }

    "get user" in {
      Get("/users/somerandomuser") ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }

    "delete user" in {
      Delete("/users/somerandomuser") ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }

    "reset user password" in {
      Post("/users/randomuser/reset-password") ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }

    "block user" in {
      Post("/users/randomuser/block") ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }

    "unblock user" in {
      Post("/users/randomuser/unblock") ~> api.routes ~> check {
        status === StatusCodes.NotImplemented
      }
    }

  }

}