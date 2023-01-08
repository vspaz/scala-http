package org.vspaz.http

import sttp.client3.{Identity, Response}

class ResponseObject(response: Identity[Response[String]]) {

  def headers()

  def fromJson()

  def statusCode()

  def asString()

  def asBytes()

}
