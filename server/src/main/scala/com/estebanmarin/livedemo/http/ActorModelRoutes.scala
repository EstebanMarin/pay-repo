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
import com.stripe.param.treasury.InboundTransferFailParams.FailureDetails.Code
import io.circe.Codec

class ActorModelRoutes[F[_]: Concurrent] private (actorM: ActorModel[F]) extends Http4sDsl[F] {
  private val prefix = "/actorModel"

  case class MessageRequest(message: String) derives Codec.AsObject

  private val demoSimpleActorRoute: HttpRoutes[F] = HttpRoutes.of[F] { case _ @GET -> Root =>
    for {
      _        <- actorM.demoSimpleActor("actorModel")
      response <- Ok("actorModel")
    } yield response

//     ❯ http POST "http://localhost:4041/actorModel" message="Hello Esteban"
// HTTP/1.1 200 OK
// Connection: keep-alive
// Content-Length: 33
// Content-Type: application/json
// Date: Sat, 06 Jul 2024 01:40:01 GMT

// "Received message: Hello Esteban"
// INFO com.estebanmarin.livedemo.core.ActorModelLive - Happy: Hello Esteban

    case req @ POST -> Root =>
      for {
        messageReq <- req.as[MessageRequest] // Decode the request body into MessageRequest
        _          <- actorM.demoSimpleActor(messageReq.message)
        response   <- Ok(s"Received message: ${messageReq.message}")
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
