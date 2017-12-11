package users.utils

import java.time.OffsetDateTime

import io.circe.syntax._
import org.specs2.mutable.Specification

import users.domain._

class CirceHelperSpec extends Specification {

  import CirceHelper._

  "CirceHelper" should {
    "encode and decode value types" in {
      val email = EmailAddress("someone@paidy.com")
      val encoded = email.asJson
      val decoded = encoded.as[EmailAddress]
      decoded must beRight(email)
    }

    "encode and decode OffsetDateTime" in {
      val now = OffsetDateTime.now()
      val encoded = now.asJson
      val decoded = encoded.as[OffsetDateTime]
      decoded must beRight(now)
    }
  }

}