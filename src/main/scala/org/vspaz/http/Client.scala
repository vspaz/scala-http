package org.vspaz.http

import scala.concurrent.duration.{Duration, SECONDS}
import sttp.client3._
import sttp.model.Method

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

  def doGet(
    endpoint: String,
    headers: Option[Map[String, String]] = None
  ): Identity[Response[String]] = buildRequest(
    method = Method.GET,
    endpoint = endpoint,
    headers = headers
  ).send(http)

  def doPost(
    endpoint: String,
    headers: Option[Map[String, String]] = None,
    payload: String = ""
  ): Identity[Response[String]] = buildRequest(
    method = Method.POST,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  ).send(http)

  def doDelete(
    endpoint: String,
    headers: Option[Map[String, String]] = None
  ): Identity[Response[String]] = buildRequest(
    method = Method.DELETE,
    endpoint = endpoint,
    headers = headers
  ).send(http)

  override def toString: String =
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
}
