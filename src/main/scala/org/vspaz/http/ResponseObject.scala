package org.vspaz.http

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import sttp.client3.{Identity, Response}

abstract class ResponseObject(response: Identity[Response[String]]) {

  val deserializer: JsonMapper = JsonMapper.builder().build()
  deserializer.registerModule(DefaultScalaModule)
  def headers() = response.headers

  def isOk() = response.is200

  def fromJson[T](valueType: T): T = deserializer.readValue(response.body, classOf[T])

  def statusCode() = response.code.code

  def asString() = response.body

  def asBytes() = response.body.getBytes
}
