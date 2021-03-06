package users.api.domain

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse, MediaTypes, StatusCode, StatusCodes}
import akka.util.ByteString
import io.circe.{Encoder, Json}
import io.circe.syntax._

import scala.language.implicitConversions

sealed trait HttpError {
  val code: StatusCode
  val details: Json
}

final case object NotFound extends HttpError {
  override val code = StatusCodes.NotFound
  override val details = Json.Null
}

final case class Conflict(reason: String) extends HttpError {
  override val code = StatusCodes.Conflict
  override val details = Map("reason" -> reason).asJson
}

final case object InternalServerError extends HttpError {
  override val code = StatusCodes.InternalServerError
  override val details = Json.Null
}

object HttpError {
  implicit val encoder: Encoder[HttpError] = Encoder.instance { error =>
    Map(
      "status_code" -> error.code.intValue.asJson,
      "reason" -> error.code.reason.asJson,
      "details" -> error.details
    ).asJson
  }

  implicit class HttpErrorImplicits(val underlying: HttpError) {
    def asHttpResponse: HttpResponse = {
      val entity = HttpEntity(
        contentType = ContentType(MediaTypes.`application/json`),
        data = ByteString(underlying.asJson.noSpaces.getBytes)
      )
      HttpResponse(status = underlying.code, entity = entity)
    }
  }

  implicit def toResponseMarshallable(error: HttpError): ToResponseMarshallable =
    error.asHttpResponse
}

object AdminApiHttpError {
  import users.services.usermanagement.Error

  implicit class ImplicitMappings(val underlying: Error) {
    def asHttpError: HttpError = mapFromUserManagementError(underlying)
  }

  def mapFromUserManagementError: PartialFunction[Error, HttpError] = {
    case Error.Exists    => Conflict(reason = "already exists")
    case Error.NotFound  => NotFound
    case Error.Active    => Conflict(reason = "user is active")
    case Error.Deleted   => Conflict(reason = "user is deleted")
    case Error.Blocked   => Conflict(reason = "user is blocked")
    case Error.System(_) => InternalServerError
  }
}

object PublicApiHttpError {
  import users.services.usermanagement.Error

  implicit class ImplicitMappings(val underlying: Error) {
    def asHttpError: HttpError = mapFromUserManagementError(underlying)
  }

  def mapFromUserManagementError: PartialFunction[Error, HttpError] = {
    case Error.Exists    => Conflict(reason = "username already taken")
    case Error.NotFound  => NotFound
    case Error.Deleted   => Conflict("can't update deleted account")
    case _ => InternalServerError
  }
}
