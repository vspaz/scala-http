package org.vspaz.http

import org.scalatest.funsuite.AnyFunSuite
import sttp.model.MediaType
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.vspaz.http.setup.ServerMockResponse

class ClientTest extends AnyFunSuite with ServerMockResponse {

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
    val resp = client.doGet(endpoint = "/test-get-method")
    assertTrue(resp.isOk())
    assertEquals(200, resp.statusCode)
    assertEquals("Ok", resp.toString())
    assertEquals(1, resp.headers.size)
    assertEquals(("test", "get"), resp.headers.head)
  }

  test("Client.doHeadOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-head-client",
        backend = Option(testHttpBackend)
      )
    val resp = client.doHead(endpoint = "/test-head-method")
    assertTrue(resp.isOk())
    assertEquals(200, resp.statusCode)
    assertEquals("Ok", resp.toString())
    assertEquals(1, resp.headers.size)
    assertEquals(("test", "head"), resp.headers.head)
  }

  test("Client.doDeleteOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-delete-client",
        backend = Option(testHttpBackend)
      )
    val resp = client.doDelete(endpoint = "/test-delete-method")
    assertTrue(resp.isSuccess())
    assertEquals(202, resp.statusCode)
    assertEquals("accepted", resp.toString())
  }

  test("Client.doPostOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-post-client",
        backend = Option(testHttpBackend)
      )
    val resp = client.doPost(
      endpoint = "/test-post-method",
      headers = Map("Content-Type" -> MediaType.ApplicationJson.toString()),
      payload = Map("test" -> "json_post_method")
    )
    assertTrue(resp.isSuccess())
    val decodedBody = resp.fromJson(classOf[Map[String, String]])
    assertEquals(Map("test" -> "json_post_method"), decodedBody)

    assertEquals(1, resp.headers.size)
    assertEquals(("test", "post"), resp.headers.head)
  }
  test("Client.doPutOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-put-client",
        backend = Option(testHttpBackend)
      )
    val resp = client.doPut(
      endpoint = "/test-put-method",
      headers = Map("Content-Type" -> MediaType.ApplicationJson.toString()),
      payload = Map("test" -> "json_put_method")
    )
    assertTrue(resp.isSuccess())
    assertEquals(202, resp.statusCode)
    val decodedBody = resp.fromJson(classOf[Map[String, String]])
    assertEquals(Map("test" -> "json_put_method"), decodedBody)

    assertEquals(1, resp.headers.size)
    assertEquals(("test", "put"), resp.headers.head)
  }

  test("Client.doPatchOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-patch-client",
        backend = Option(testHttpBackend)
      )
    val resp = client.doPatch(
      endpoint = "/test-patch-method",
      headers = Map("Content-Type" -> MediaType.ApplicationJson.toString()),
      payload = Map("test" -> "json_patch_method")
    )
    assertTrue(resp.isSuccess())
    assertEquals(202, resp.statusCode)
    val decodedBody = resp.fromJson(classOf[Map[String, String]])
    assertEquals(Map("test" -> "json_patch_method"), decodedBody)

    assertEquals(1, resp.headers.size)
    assertEquals(("test", "patch"), resp.headers.head)
  }
  test("TestResponseHeadersOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        backend = Option(testHttpBackend)
      )
    val resp = client.doGet(endpoint = "/test-response-headers")
    assertTrue(resp.isOk())
    assertEquals(("header1", "value1"), resp.headers.head)
    assertEquals(("header2", "value2"), resp.headers(1))
    assertEquals(("header3", "value3"), resp.headers(2))
  }
}
