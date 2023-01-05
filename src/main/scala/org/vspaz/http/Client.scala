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
  private val responseTimeout = Duration(readTimeout, SECONDS);
  private val http = backend.getOrElse(
    HttpClientSyncBackend(options =
      SttpBackendOptions.connectionTimeout(Duration(connectionTimeout, SECONDS))
    )
  );
  def buildRequest(
    method: Method,
    url: String,
    headers: Option[Map[String, String]] = None
  ): RequestT[Identity, String, Any] = {
    val request = basicRequest
      .readTimeout(responseTimeout)
      .method(method, uri = uri"${host + url}")
      .response(asStringAlways)
    if (basicAuthUser != "" && basicUserPassword != "")
      request.auth.basic(basicAuthUser, basicUserPassword)
    val allHeaders = headers.getOrElse(Map()) ++ Map("User-Agent" -> userAgent)
    request.headers(allHeaders)
    request
  }

  def doGet(uri: String, headers: Option[Map[String, String]] = None): Identity[Response[String]] =
    buildRequest(Method.GET, uri, headers).send(http)

  override def toString: String =
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
}
