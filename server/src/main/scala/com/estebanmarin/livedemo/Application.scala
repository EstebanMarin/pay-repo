package com.estebanmarin.livedemo

import cats.effect.*
import doobie.util.ExecutionContexts
import doobie.hikari.HikariTransactor
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS

import com.estebanmarin.livedemo.core.*
import com.estebanmarin.livedemo.http.*
import doobie.util.transactor.Transactor
import com.zaxxer.hikari.HikariDataSource
import org.http4s.server.Server

import cats.effect.IO
import cats.effect.Resource
import scala.concurrent.ExecutionContext.global

import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.Client

import cats.implicits._
import cats.syntax.semigroupk._ // Import SemigroupK syntax

object Application extends IOApp.Simple {
  def makePostgres: Resource[IO, Transactor[IO] { type A = HikariDataSource }] = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](32)
    transactor <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5444/",
      "docker",
      "docker",
      ec
    )
  } yield transactor

  def makeClient: Resource[IO, Client[IO]] =
    EmberClientBuilder.default[IO].build

  def makeServer: Resource[IO, Server] = for {
    postgres: Transactor[IO] { type A = HikariDataSource } <-
      makePostgres
    actorModel: ActorModel[IO]          <- ActorModelLive.resource[IO]
    jobs: JobsLive[IO]                  <- JobsLive.resource[IO](postgres)
    jobApi: JobRoutes[IO]               <- JobRoutes.resource[IO](jobs)
    actorModelApi: ActorModelRoutes[IO] <- ActorModelRoutes.resource[IO](actorModel)

    routesAll = jobApi.routes <+> actorModelApi.routes
    server <- EmberServerBuilder
      .default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"4041")
      .withHttpApp(CORS(routesAll.orNotFound))
      .build
  } yield server

  override def run: IO[Unit] =
    makeServer.use(_ => IO.println("Esteban Marin Server ready.") *> IO.never)
}
