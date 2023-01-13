# scala-http

a simple HTTP client that's simple to configure & use.
it provides a simple configuration for:
- json serialization & deserialization
- basic auth
- delayed retries on specific HTTP errors & HTTP exceptions
- timeouts (connection, read, idle etc.)
- extra helpers
- logging
etc.

### How to create a client

```scala

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

```