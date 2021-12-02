package controllers

import play.api.mvc._
import cats.effect.std._

trait FActionBuilder[F[_], +R[_], B] {
  def apply[A](bodyParser: BodyParser[A]): FActionBuilder[F, R, A]
  def apply(block: R[B] => F[Result]): Action[B]
}

class FActionBuilderImpl[F[_], R[_], B](underlying: ActionBuilder[R, B], dispatcher: Dispatcher[F]) extends FActionBuilder[F, R, B] {
  def apply[A](bodyParser: BodyParser[A]): FActionBuilder[F, R, A] = new FActionBuilderImpl(underlying(bodyParser), dispatcher)
  def apply(block: R[B] => F[Result]): Action[B] = underlying.async { req =>
    dispatcher.unsafeToFuture(block(req))
  }
}

object FActionBuilder {
  type Default[F[_]] = FActionBuilder[F, Request, AnyContent]
  def default[F[_]](defaultActionBuilder: DefaultActionBuilder, dispatcher: Dispatcher[F]): Default[F] = new FActionBuilderImpl[F, Request, AnyContent](defaultActionBuilder, dispatcher)
}
