package org.vspaz.http

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import sttp.client3.Response


class ResponseWrapper(response: Response[String]) {

  val deserializer: JsonMapper = JsonMapper.builder().build()
  deserializer.registerModule(DefaultScalaModule)

  def headers = {
    (for (header <- response.headers) yield (header.name, header.value)).toList
  }

  def isClientError(): Boolean = {
    response.isClientError
  }

  def isServerError(): Boolean = {
    response.isServerError
  }

  def isOk(): Boolean = response.is200

  def isSuccess(): Boolean = response.isSuccess

  def fromJson[T](valueType: Class[T]): T = deserializer.readValue(response.body, valueType)

  def statusCode: Int = response.code.code

  override def toString(): String = response.body

  def toBytes(): Array[Byte] = response.body.getBytes
}
