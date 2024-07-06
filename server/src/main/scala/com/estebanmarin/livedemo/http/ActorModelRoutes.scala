package com.estebanmarin.livedemo.http

import cats.effect.*
import cats.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec.*
import io.circe.generic.auto.*

import com.estebanmarin.livedemo.core.*
import com.estebanmarin.livedemo.domain.job.*
import org.http4s.server.Router

class ActorModelRoutes[F[_]: Concurrent] private (actorM: ActorModel[F]) extends Http4sDsl[F] {
  private val prefix = "/actorModel"

  private val demoSimpleActorRoute: HttpRoutes[F] = HttpRoutes.of[F] { case _ @GET -> Root =>
    for {
      _        <- actorM.demoSimpleActor
      response <- Ok("actorModel")
    } yield response
  }

  val routes: HttpRoutes[F] = Router(
    prefix -> demoSimpleActorRoute
  )
}

object ActorModelRoutes {
  def resource[F[_]: Concurrent](actorM: ActorModel[F]): Resource[F, ActorModelRoutes[F]] =
    Resource.pure(new ActorModelRoutes[F](actorM))
}
