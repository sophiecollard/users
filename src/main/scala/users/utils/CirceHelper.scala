package users.utils

import java.time.OffsetDateTime

import cats.implicits._
import io.circe.{Decoder, DecodingFailure, Encoder}
import io.circe.syntax._
import shapeless._

object CirceHelper {
  implicit def encodeAnyVal[T, V](
    implicit ev: T <:< AnyVal,
    unwrapped: Unwrapped.Aux[T, V],
    encoderUnwrapped: Encoder[V]
  ): Encoder[T] = Encoder.instance[T] { v =>
    encoderUnwrapped(unwrapped.unwrap(v))
  }

  implicit def decoderAnyVal[T <: AnyVal, V](
    implicit gen: Lazy[Generic.Aux[T, V :: HNil]],
    decoder: Decoder[V]
  ): Decoder[T] = Decoder.instance { cursor ⇒
    decoder(cursor).map { value ⇒
      gen.value.from(value :: HNil)
    }
  }

  implicit val offsetDateTimeEncoder: Encoder[OffsetDateTime] =
    Encoder.instance[OffsetDateTime](_.toString.asJson)

  implicit val offsetDateTimeDecoder: Decoder[OffsetDateTime] =
    Decoder.instance { c =>
      c.as[String] fold( { _ =>
        Either.left(DecodingFailure("DateTimeOffset", c.history))
      }, { s =>
        try {
          Either.right(OffsetDateTime.parse(s))
        } catch { case _: IllegalArgumentException =>
          Either.left(DecodingFailure("expected DateTimeOffset ISO 8601", c.history))
        }
      })
    }
}