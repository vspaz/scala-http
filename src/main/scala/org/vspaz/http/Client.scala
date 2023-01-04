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
  def buildRequest(method: String, uri: String): RequestT[Identity, String, Any] = {
    val url = host + uri
    val request = basicRequest
      .headers(Map("User-Agent" -> userAgent))
      .readTimeout(responseTimeout)
      .method(Method(method.capitalize), uri = uri"$host$url")
      .response(asStringAlways)
    if (basicAuthUser != "" && basicUserPassword != "")
      request.auth.basic(basicAuthUser, basicUserPassword)
    request
  }

  override def toString: String =
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
}
