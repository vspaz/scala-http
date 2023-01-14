package org.vspaz

import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.vspaz.http.Client

case class Response(
                     args: Option[Map[String, String]],
                     headers: Option[Map[String, String]],
                     origin: Option[String],
                     url: Option[String]) {}

object Main {
  private def doSimpleGetExample(): Unit = {
    val resp = new Client().doGet(endpoint="https://httpbin.org/get")
    assertTrue(resp.isOk())

    val decodedBody = resp.fromJson(classOf[Response])
    assertEquals("https://httpbin.org/get", decodedBody.url.get)
  }

  private def doSimpleGetExampleWithQueryParams(): Unit = {
    val resp = new Client().doGet(endpoint = "https://httpbin.org/get", params = Map("foo" -> "bar"))
    assertTrue(resp.isOk())
    val decodedBody = resp.fromJson(classOf[Response])
    assertEquals("https://httpbin.org/get?foo=bar", decodedBody.url.get)
    assertEquals("bar", decodedBody.args.get("foo"))
  }

  def main(args: Array[String]): Unit = {
    doSimpleGetExample()
    doSimpleGetExampleWithQueryParams()
  }
}