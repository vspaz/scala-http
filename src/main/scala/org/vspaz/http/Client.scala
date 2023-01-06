package org.vspaz.http

import scala.concurrent.duration.{Duration, SECONDS}
import sttp.client3.{Identity, _}
import sttp.model.Method

import System.currentTimeMillis

class Client(
  host: String = "",
  userAgent: String = "",
  basicAuthUser: String = "",
  basicUserPassword: String = "",
  token: String = "",
  retryCount: Int = 3,
  retryOnErrors: Set[Int] = Set(),
  delay: Int = 2,
  readTimeout: Int = 10,
  connectionTimeout: Int = 10,
  logger: Option[ILogger] = None,
  backend: Option[SttpBackend[Identity, Any]] = None
) {
  private val responseTimeout = Duration(readTimeout, SECONDS)
  private val http = backend.getOrElse(
    HttpClientSyncBackend(options =
      SttpBackendOptions.connectionTimeout(Duration(connectionTimeout, SECONDS))
    )
  )
  private def buildRequest(
    method: Method,
    endpoint: String,
    headers: Option[Map[String, String]] = None,
    payload: String = ""
  ): RequestT[Identity, String, Any] = {
    val request = basicRequest
      .headers(headers.getOrElse(Map()) ++ Map("User-Agent" -> userAgent))
      .readTimeout(responseTimeout)
      .method(method, uri = uri"${host + endpoint}")
      .body(payload)
      .response(asStringAlways)
    if (basicAuthUser != "" && basicUserPassword != "")
      request.auth.basic(basicAuthUser, basicUserPassword)
    request
  }

  private def doRequest(
    method: Method = Method.GET,
    endpoint: String,
    headers: Option[Map[String, String]] = None,
    payload: Option[String] = None
  ): RequestT[Identity, String, Any] = timeIt(
    buildRequest(method = method, endpoint = endpoint, headers = headers, payload.getOrElse(""))
  )

  def doGet(
    endpoint: String,
    headers: Option[Map[String, String]] = None
  ): Identity[Response[String]] = doRequest(
    method = Method.GET,
    endpoint = endpoint,
    headers = headers
  ).send(http)

  def doPost(
    endpoint: String,
    headers: Option[Map[String, String]] = None,
    payload: Option[String] = None
  ): Identity[Response[String]] = doRequest(
    method = Method.POST,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  ).send(http)

  def doPatch(
    endpoint: String,
    headers: Option[Map[String, String]] = None,
    payload: Option[String] = None
  ): Identity[Response[String]] = doRequest(
    method = Method.PATCH,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  ).send(http)

  def doPut(
    endpoint: String,
    headers: Option[Map[String, String]] = None,
    payload: Option[String] = None
  ): Identity[Response[String]] = doRequest(
    method = Method.PUT,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  ).send(http)

  def doDelete(
    endpoint: String,
    headers: Option[Map[String, String]] = None
  ): Identity[Response[String]] = doRequest(
    method = Method.DELETE,
    endpoint = endpoint,
    headers = headers
  ).send(http)

  private def timeIt[T](expression: => T): T = {
    val start = currentTimeMillis()
    val result = expression
    println(s"request took {$currentTimeMillis() - $start} to complete")
    result
  }

  override def toString: String =
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
}
