package users

import cats.data._
import cats.implicits._
import com.typesafe.config.ConfigFactory
import pureconfig.syntax._

import users.config._
import users.main._

object Main extends App {

  val config = ConfigFactory.load().to[ApplicationConfig].get // not safe!

  val application = Application.fromApplicationConfig.run(config)

}
