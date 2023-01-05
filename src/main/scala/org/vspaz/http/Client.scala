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
    url: String,
    headers: Option[Map[String, String]] = None,
    payload: String = ""
  ): RequestT[Identity, String, Any] = {
    val allHeaders = headers.getOrElse(Map()) ++ Map("User-Agent" -> userAgent)
    val request = basicRequest
      .headers(allHeaders)
      .readTimeout(responseTimeout)
      .method(method, uri = uri"${host + url}")
      .body(payload)
      .response(asStringAlways)
    if (basicAuthUser != "" && basicUserPassword != "")
      request.auth.basic(basicAuthUser, basicUserPassword)
    request
  }

  def doGet(uri: String, headers: Option[Map[String, String]] = None): Identity[Response[String]] =
    buildRequest(Method.GET, uri, headers).send(http)

  def doPost(
    uri: String,
    headers: Option[Map[String, String]] = None,
    payload: String = ""
  ): Identity[Response[String]] = buildRequest(
    method = Method.GET,
    url = uri,
    headers = headers,
    payload = payload
  ).send(http)

  def doDelete(
    uri: String,
    headers: Option[Map[String, String]] = None
  ): Identity[Response[String]] = buildRequest(Method.DELETE, uri, headers).send(http)

  override def toString: String =
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
}
