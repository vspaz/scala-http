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

#### Init client with default parameters

```scala
package org.vspaz


import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}

import org.vspaz.http.Client

object Main {
  def main(args: Array[String]): Unit = {
    val resp = new Client().doGet("https://example.com/some-endpoint")
    
    assertTrue(resp.isOk())
    assertEquals(200, resp.statusCode)
    assertEquals(("header1", "value1"), resp.headers.head)
  }
}
```

#### Init client with various parameters

```scala

package org.vspaz


import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}

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
      retryDelay = 1,
      connectionTimeout = 5,
      readTimeout = 10
    )
  }

  val resp = client.doGet("/some-endpoint")
  assertTrue(resp.isOk())
  assertEquals(200, resp.statusCode)
  assertEquals(("header1", "value1"), resp.headers.head)
}

```