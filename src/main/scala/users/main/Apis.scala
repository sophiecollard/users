package users.main

import cats.data._

import users.api.usermanagement._
import users.config._

object Apis {
  val reader: Reader[(Executors, Services), Apis] = Reader((Apis.apply _).tupled)

  val fromApplicationConfig: Reader[ApplicationConfig, Apis] =
    (for {
      executors ← Executors.fromApplicationConfig
      services ← Services.fromApplicationConfig
    } yield (executors, services)) andThen reader
}

final case class Apis(
    executors: Executors,
    services: Services
) {
  import executors._

  implicit val ec = apisExecutor

  val adminUserManagement = new AdminApi()

  val routes = adminUserManagement.routes
}