package users.main

import cats.data._

import users.config._

object Application {

  val reader: Reader[Apis, Application] =
    Reader(Application.apply)

  val fromApplicationConfig: Reader[ApplicationConfig, Application] =
    Apis.fromApplicationConfig andThen reader
}

case class Application(
    apis: Apis
)
