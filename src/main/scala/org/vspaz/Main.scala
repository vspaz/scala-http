package org.vspaz

import org.vspaz.http.Client

object Main {
  def main(args: Array[String]): Unit = {
    val client = new Client(
      host= "https://example.com",
      userAgent="client-name-and-version",
      basicAuthUser="user",
      basicUserPassword = "pass",
      retryCount = 3,
      retryOnErrors = Set(400, 500, 503),
      retryOnExceptions = Set(
        "sttp.client3.SttpClientException.TimeoutException",
        "java.lang.RuntimeException",
        "java.lang.Throwable"),
      delay = 1,
      connectionTimeout = 5,
      readTimeout = 10,
    )
  }
}