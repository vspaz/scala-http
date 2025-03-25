package org.vspaz.http

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import sttp.client3.Response

import scala.collection.mutable

class ResponseWrapper(response: Response[String]) {

  val deserializer: JsonMapper = JsonMapper.builder().addModule(DefaultScalaModule).build()

  def headers: mutable.Map[String, String] = {
    var headerToValues = scala.collection.mutable.Map[String, String]()
    for (header <- response.headers)
      if (!headerToValues.contains(header.name))
        headerToValues(header.name) = header.value
      else {
        val existingHeaderValue = headerToValues(header.name)
        headerToValues(header.name) = s"$existingHeaderValue;${header.value}"
      }
    headerToValues
  }

  def isClientError(): Boolean = response.isClientError

  def isServerError(): Boolean = response.isServerError

  def isOk(): Boolean = response.is200

  def isSuccess(): Boolean = response.isSuccess

  def fromJson[T](valueType: Class[T]): T = deserializer.readValue(response.body, valueType)

  def statusCode: Int = response.code.code

  override def toString(): String = response.body

  def toBytes(): Array[Byte] = response.body.getBytes
}
