package users.main

import cats.data._

import users.api.usermanagement._
import users.config._

object Apis {
  val reader: Reader[Services, Apis] = Reader(Apis.apply)

  val fromApplicationConfig: Reader[ApplicationConfig, Apis] =
    Services.fromApplicationConfig andThen reader
}

final case class Apis(
    services: Services
) {
  val adminUserManagement = new AdminApi()

  val routes = adminUserManagement.routes
}