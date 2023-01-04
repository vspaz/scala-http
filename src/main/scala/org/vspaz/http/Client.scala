package org.vspaz.http

import scala.concurrent.duration.{Duration, SECONDS}
import sttp.client3._

class Client(
  host: String = "",
  userAgent: String = "",
  basicAuthUser: String = "",
  basicUserPassword: String = "",
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

  override def toString: String =
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
}
