package org.vspaz

import org.vspaz.http.Client

object Main {
  def main(args: Array[String]): Unit = {
    val client = new Client(
      host= "https://example.com",
      userAgent="client",
      basicAuthUser="user",
      basicUserPassword = "pass",
      retryCount = 3,
      retryOnErrors = Set(),
      retryOnExceptions = Set(),
      delay = 1,
      connectionTimeout = 5,
      readTimeout = 10,
    )
  }
}