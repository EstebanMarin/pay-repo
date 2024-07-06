package com.estebanmarin.livedemo.models

import io.circe.Codec

object models {
  case class MessageRequest(message: String) derives Codec.AsObject

}
