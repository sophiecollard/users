package users.main

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.data._

import users.config._

import scala.io.StdIn

object Application {

  val reader: Reader[Apis, Application] =
    Reader(Application.apply)

  val fromApplicationConfig: Reader[ApplicationConfig, Application] =
    Apis.fromApplicationConfig andThen reader
}

case class Application(
    apis: Apis
) {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val bindingFuture = Http().bindAndHandle(apis.routes, "localhost", 8080)

  println(s"Server started at http://localhost:8080\nPress RETURN to stop...")
  StdIn.readLine() // run until the user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate)
}
