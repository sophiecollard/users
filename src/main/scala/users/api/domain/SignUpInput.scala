package users.api.domain

import io.circe.{Encoder, Decoder}
import io.circe.generic.semiauto._

import users.domain._
import users.utils.CirceHelper._

case class SignUpInput(
    userName: UserName,
    emailAddress: EmailAddress,
    password: Password
)

object SignUpInput {
  implicit val encoder: Encoder[SignUpInput] = deriveEncoder
  implicit val decoder: Decoder[SignUpInput] = deriveDecoder
}