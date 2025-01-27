package com.estebanmarin.livedemo.core

import cats.effect.*
import cats.implicits.*
import cats.*
import org.apache.pekko.actor.typed.{Behavior, ActorSystem}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

trait ActorModel[F[_]] {
  def useActorModel: F[String]
  def demoSimpleActor(msg: String): F[Unit]
}

class ActorModelLive[F[_]: Monad] extends ActorModel[F] {
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

  object SimpleActorBehavior_v2 {
    def apply(): Behavior[String] = Behaviors.receive((context, msg) => {
      // println(s"Received message: Actor")
      context.log.info(s"Received message: $msg")
      Behaviors.same
    })
  }

  object SimpleActorBehavior_v3 {
    def apply(): Behavior[String] = Behaviors.setup { context =>
      context.log.info("SimpleActor started")
      // first message
      Behaviors.receiveMessage((msg: String) => {
        context.log.info(s"Received message: $msg")
        Behaviors.same
      })
    }
  }

  // val simpleActor: ActorSystem[String] = ActorSystem(SimpleActorBehavior_v3(), "simpleActor")
  val simpleActor: ActorSystem[String] = ActorSystem(Person.happy(), "simpleActor")

  def demoSimpleActor(msg: String): F[Unit] = {
    // part 2 instanciate an actor system
    // part 3 send a message to the actor
    (simpleActor ! msg).pure[F]
  }

  // exercise

  object Person:
    def happy(): Behavior[String] = Behaviors.receive { (context, msg) =>
      context.log.info(s"Logging from Happy: $msg")
      msg match {
        case "pekko sad" => sad()
        case _           => Behaviors.same
      }
    }

    def sad(): Behavior[String] = Behaviors.receive { (context, msg) =>
      context.log.info(s"Sad: $msg")
      msg match {
        case "pekko happy" => happy()
        case _             => Behaviors.same
      }
    }
  def useActorModel: F[String] = "use ActorModel".pure[F]
}

object ActorModelLive {
  def make[F[_]: Monad]: F[ActorModel[F]] = new ActorModelLive[F].pure[F]
  def resource[F[_]: Monad]: Resource[F, ActorModel[F]] =
    Resource.pure(new ActorModelLive[F])
}
