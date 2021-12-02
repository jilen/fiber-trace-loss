package loader

import cats.effect._
import cats.effect.std._
import cats.effect.unsafe.implicits.global
import controllers._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.routing._
import play.api.routing.sird._

class AppLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    new AppComponents(context).application
  }
}

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) {
  // set up logger
  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment)
  }

  lazy val httpFilters = Seq()

  val router = Router.from {
    case GET(p"/dump/fibers") =>
      dumpController.dumpFibers

  }

  private lazy val (dispatcher, releaseDispatcher) = Dispatcher[IO].allocated.unsafeRunSync()

  val defaultFAction = FActionBuilder.default(defaultActionBuilder, dispatcher)
  val dumpController = new DumpController(defaultFAction)
  val dumpToken = dumpController.peoridicDump.start.unsafeRunSync()

  applicationLifecycle.addStopHook { () =>
    releaseDispatcher.unsafeToFuture()
  }

  applicationLifecycle.addStopHook { () =>
    dumpToken.cancel.unsafeToFuture()
  }

}


class AppEnv[F[_]](
  dispatcher: Dispatcher[F],
  stopSignal: Deferred[F, Either[Throwable, Unit]]
)
