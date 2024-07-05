package com.estebanmarin.livedemo.core

trait DummyClient[F[_]] {
  def getRestTest: F[String]
}

class DummyClientLive[F[_]] extends DummyClient[F] {
  def getRestTest: F[String] = ???
}
