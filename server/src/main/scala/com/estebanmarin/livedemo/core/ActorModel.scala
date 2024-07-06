package com.estebanmarin.livedemo.core

import cats.effect.*
import cats.syntax.all.*
import cats.implicits.*
import cats.Applicative
import org.apache.pekko.actor.typed.{Behavior, ActorSystem}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.checkerframework.checker.units.qual.m

trait ActorModel[F[_]] {
  def useActorModel: F[String]
  def demoSimpleActor: F[Unit]
}

class ActorModelLive[F[_]: Applicative] extends ActorModel[F] {
  // part 1 define a behevior of an actor
  val simpleActorBehavior: Behavior[String] = Behaviors.receiveMessage((msg: String) => {
    println(s"Received message: $msg")
    Behaviors.same
  })

  object SimpleActorBehavior {
    def apply(): Behavior[String] = Behaviors.receiveMessage((msg: String) => {
      println(s"Received message: Actor")
      Behaviors.same
    })
  }

//   object SimpleActorBehavior2 {
//     def apply(): Behavior[String] = Behaviors.receive((context, msg) => {
//       println(s"Received message: Actor")
//       Behaviors.same})
//     }
//  }

  val simpleActor: ActorSystem[String] = ActorSystem(SimpleActorBehavior(), "simpleActor")

  def demoSimpleActor: F[Unit] = {
    // part 2 instanciate an actor system
    // part 3 send a message to the actor
    (simpleActor ! "Hello, World!").pure[F]
  }

  def useActorModel: F[String] = "use ActorModel".pure[F]
}

object ActorModelLive {
  def make[F[_]: Applicative]: F[ActorModel[F]]               = new ActorModelLive[F].pure[F]
  def resource[F[_]: Applicative]: Resource[F, ActorModel[F]] = Resource.pure(new ActorModelLive[F])
}
