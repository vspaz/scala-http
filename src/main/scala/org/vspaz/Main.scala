package org.vspaz


import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.vspaz.http.Client

case class Response(
                     args: Option[Map[String, String]],
                     headers: Option[Map[String, String]],
                     origin: Option[String],
                     url: Option[String]) {}

object Main {
  // do simple GET request
  private def doSimpleGetExample(): Unit = {
    val resp = new Client().doGet(endpoint="https://httpbin.org/get")
    assertTrue(resp.isOk())
    assertTrue(resp.isSuccess())
    assertEquals(200, resp.statusCode)
  }

  // deserialize JSON payload
  private def doSimpleGetWithJsonDeserializationExample(): Unit = {
    val resp = new Client().doGet(endpoint="https://httpbin.org/get")
    assertTrue(resp.isOk())

    val decodedBody = resp.fromJson(classOf[Response])
    assertEquals("https://httpbin.org/get", decodedBody.url.get)
  }

  // add query params to request
  private def doSimpleGetWithQueryParamsExample(): Unit = {
    val resp = new Client().doGet(endpoint = "https://httpbin.org/get", params = Map("foo" -> "bar"))
    assertTrue(resp.isOk())

    val decodedBody = resp.fromJson(classOf[Response])
    assertEquals("https://httpbin.org/get?foo=bar", decodedBody.url.get)
    assertEquals("bar", decodedBody.args.get("foo"))
  }

  // add headers to request
  private def doSimpleGetWithHeadersExample(): Unit = {
    val resp = new Client().doGet(endpoint = "https://httpbin.org/get", headers = Map("Header-Type" -> "header-value"))
    assertTrue(resp.isOk())

    val decodedBody = resp.fromJson(classOf[Response])
    assertEquals("header-value", decodedBody.headers.get("Header-Type"))
  }

  def main(args: Array[String]): Unit = {
    doSimpleGetExample()
    doSimpleGetWithJsonDeserializationExample()
    doSimpleGetWithQueryParamsExample()
    doSimpleGetWithHeadersExample()
  }
}