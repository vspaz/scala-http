package org.vspaz.http

import scala.concurrent.duration.{Duration, SECONDS};

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
    logger: Option[ILogger]
) {
  private val responseTimeout = Duration(readTimeout, SECONDS);
  private val connTimeout = Duration(connectionTimeout, SECONDS);

  override def toString: String = {
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
  }
}
