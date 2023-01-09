package org.vspaz.http

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import sttp.client3.{Identity, Response}

class ResponseWrapper(response: Identity[Response[String]]) {

  val deserializer: JsonMapper = JsonMapper.builder().build()
  deserializer.registerModule(DefaultScalaModule)
  def headers() = response.headers

  def isOk() = response.is200

  def fromJson[T](valueType: Class[T]): T = deserializer.readValue(response.body, valueType)

  def statusCode() = response.code.code

  def asString() = response.body

  def asBytes() = response.body.getBytes
}
