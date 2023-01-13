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
        "java.lang.Throwable",
      ),
      retryDelay = 1,
      connectionTimeout = 5,
      readTimeout = 10,
    )

    var resp = client.doGet("/some-endpoint")

    // or simply
    resp = new Client().doGet("https://example.com/some-endpoint")
    assertTrue(resp.isOk())
    assertEquals(200, resp.statusCode)

    // POST
    resp = client.doPost(
      endpoint = "/test-post-method",
      headers = Map("Content-Type" -> "application/json"),
      payload = Map("header" -> "value")
    )
    assertTrue(resp.isSuccess())
  }
}