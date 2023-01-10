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
    val resp = client.doGet(endpoint = "/test-get")
    assertTrue(resp.isOk())
    assertEquals(200, resp.statusCode)
    assertEquals("Ok", resp.toString())
  }

  test("Client.doHeadOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-head-client",
        backend = Option(testHttpBackend)
      )
    val resp = client.doHead(endpoint = "/test-head")
    assertTrue(resp.isOk())
    assertEquals(200, resp.statusCode)
    assertEquals("Ok", resp.toString())
  }

  test("Client.doDeleteOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-delete-client",
        backend = Option(testHttpBackend)
      )
    val resp = client.doDelete(endpoint = "/test-delete")
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
      endpoint = "/test-post",
      headers = Map("Content-Type" -> MediaType.ApplicationJson.toString()),
      payload = Map("test" -> "json")
    )
    assertTrue(resp.isSuccess())
    val decodedBody = resp.fromJson(classOf[Map[String, String]])
    assertEquals(Map("test" -> "json"), decodedBody)
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
      endpoint = "/test-put",
      headers = Map("Content-Type" -> MediaType.ApplicationJson.toString()),
      payload = Map("test" -> "json")
    )
    assertTrue(resp.isSuccess())
    assertEquals(202, resp.statusCode)
    val decodedBody = resp.fromJson(classOf[Map[String, String]])
    assertEquals(Map("test" -> "json_put_method"), decodedBody)
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
      endpoint = "/test-patch",
      headers = Map("Content-Type" -> MediaType.ApplicationJson.toString()),
      payload = Map("test" -> "json")
    )
    assertTrue(resp.isSuccess())
    assertEquals(202, resp.statusCode)
    val decodedBody = resp.fromJson(classOf[Map[String, String]])
    assertEquals(Map("test" -> "json_patch_method"), decodedBody)
  }
}
