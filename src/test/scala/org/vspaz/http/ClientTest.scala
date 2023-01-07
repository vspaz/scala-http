package org.vspaz.http

import org.scalatest.funsuite.AnyFunSuite
import sttp.client3.{Identity, Response, StringBody}
import sttp.capabilities.WebSockets
import sttp.client3.testing.SttpBackendStub
import sttp.model.{MediaType, Method, StatusCode}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}

trait Setup {
  def getTestHttpBackendStub: SttpBackendStub[Identity, WebSockets] = SttpBackendStub
    .synchronous
    .whenRequestMatchesPartial {
      case request if request.method.equals(Method.GET) =>
        assertTrue(request.uri.path.endsWith(List("test-get")))
        assertEquals("test-get-client", request.headers().headers("User-Agent").head)
        Response("Ok", StatusCode.Ok)
      case request if request.method.equals(Method.DELETE) =>
        assertTrue(request.uri.path.endsWith(List("test-delete")))
        assertEquals("test-delete-client", request.headers().headers("User-Agent").head)
        Response("accepted", StatusCode.Accepted)
      case request if request.method.equals(Method.POST) =>
        assertTrue(request.uri.path.endsWith(List("test-post")))
        assertEquals("test-post-client", request.headers().headers("User-Agent").head)
        assertEquals(
          MediaType.ApplicationJson.toString(),
          request.headers().headers("Content-Type").head
        )
        assertEquals(StringBody("""{"test":"json"}""", "utf-8", MediaType.TextPlain), request.body)
        Response("accepted", StatusCode.Accepted)
      case request if request.method.equals(Method.PATCH) =>
        assertTrue(request.uri.path.endsWith(List("test-patch")))
        assertEquals("test-patch-client", request.headers().headers("User-Agent").head)
        assertEquals(
          MediaType.ApplicationJson.toString(),
          request.headers().headers("Content-Type").head
        )
        assertEquals(StringBody("""{"test":"json"}""", "utf-8", MediaType.TextPlain), request.body)
        Response("accepted", StatusCode.Accepted)
      case request if request.method.equals(Method.PUT) =>
        assertTrue(request.uri.path.endsWith(List("test-put")))
        assertEquals("test-put-client", request.headers().headers("User-Agent").head)
        assertEquals(
          MediaType.ApplicationJson.toString(),
          request.headers().headers("Content-Type").head
        )
        assertEquals(StringBody("""{"test":"json"}""", "utf-8", MediaType.TextPlain), request.body)
        Response("accepted", StatusCode.Accepted)
    }
}

class ClientTest extends AnyFunSuite with Setup {
  test("Client.InitOk") {
    assertEquals(
      "host: '', userAgent: '', readTimeout: '10', 'connectionTimeout: '10'",
      new Client().toString
    )
  }
  test("Client.doGetOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-get-client",
        backend = Option(testHttpBackend)
      )
    client.doGet(endpoint = "/test-get")
  }

  test("Client.doDeleteOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-delete-client",
        backend = Option(testHttpBackend)
      )
    client.doDelete(endpoint = "/test-delete")
  }

  test("Client.doPostOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-post-client",
        backend = Option(testHttpBackend)
      )
    client.doPost(
      endpoint = "/test-post",
      headers = Map("Content-Type" -> MediaType.ApplicationJson.toString()),
      payload = Map("test" -> "json")
    )
  }
  test("Client.doPutOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-put-client",
        backend = Option(testHttpBackend)
      )
    client.doPut(
      endpoint = "/test-put",
      headers = Map("Content-Type" -> MediaType.ApplicationJson.toString()),
      payload = Map("test" -> "json")
    )
  }

  test("Client.doPatchOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-patch-client",
        backend = Option(testHttpBackend)
      )
    client.doPatch(
      endpoint = "/test-patch",
      headers = Map("Content-Type" -> MediaType.ApplicationJson.toString()),
      payload = Map("test" -> "json")
    )
  }
}
