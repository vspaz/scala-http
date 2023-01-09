package org.vspaz.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.scalatest.funsuite.AnyFunSuite
import sttp.client3.{Identity, Response, StringBody, SttpClientException, UriContext, basicRequest}
import sttp.capabilities.WebSockets
import sttp.client3.testing.SttpBackendStub
import sttp.model.{MediaType, Method, StatusCode}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}

import java.lang.System.currentTimeMillis

trait Setup {
  var retryCount: Int = 0

  val serializer: JsonMapper = JsonMapper.builder().build()
  serializer.registerModule(DefaultScalaModule)

  val deserializer: ObjectMapper = new ObjectMapper()
  deserializer.registerModule(DefaultScalaModule)

  def getTestHttpBackendStub: SttpBackendStub[Identity, WebSockets] = SttpBackendStub
    .synchronous
    .whenRequestMatchesPartial {
      case request if request.uri.path.endsWith(List("retry-request")) =>
        retryCount += 1
        if (retryCount <= 1)
          throw new SttpClientException.ReadException(
            basicRequest.get(uri = uri"http://mock.api/retry-request"),
            new RuntimeException("failed to connect")
          )
        else
          assertEquals("test-retry-client", request.headers().headers("User-Agent").head)
        Response(s"retry count: '$retryCount'", StatusCode.Ok)
      case request if request.uri.path.endsWith(List("connection-exception")) =>
        throw new SttpClientException.ConnectException(
          basicRequest.get(uri = uri"http://mock.api/connect-exception"),
          new RuntimeException("failed to connect")
        )
      case request if request.uri.path.endsWith(List("read-exception")) =>
        throw new SttpClientException.ConnectException(
          basicRequest.get(uri = uri"http://mock.api/read-exception"),
          new RuntimeException("failed to read from a socket")
        )
      case request if request.uri.path.endsWith(List("timeout-exception")) =>
        throw new SttpClientException.TimeoutException(
          basicRequest.get(uri = uri"http://mock.api/timeout-exception"),
          new RuntimeException("timed occurred")
        )
      case request if request.method.equals(Method.GET) =>
        assertTrue(request.uri.path.endsWith(List("test-get")))
        assertEquals("test-get-client", request.headers().headers("User-Agent").head)
        Response("Ok", StatusCode.Ok)
      case request if request.method.equals(Method.HEAD) =>
        assertTrue(request.uri.path.endsWith(List("test-head")))
        assertEquals("test-head-client", request.headers().headers("User-Agent").head)
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
        Response(serializer.writer.writeValueAsString(Map("test" -> "json")), StatusCode.Accepted)
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
    val resp = client.doGet(endpoint = "/test-get")
    assertTrue(resp.is200)
    assertEquals("Ok", resp.body)
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
    assertTrue(resp.is200)
    assertEquals("Ok", resp.body)
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
    assertTrue(resp.isSuccess)
    assertEquals("accepted", resp.body)
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
    assertTrue(resp.isSuccess)

    val decodedBody = deserializer.readValue(resp.body, classOf[Map[String, String]])
    val response = new ResponseWrapper(resp)
    val decodedValue = response.fromJson(classOf[Map[String, String]])
    assertEquals(Map("test" -> "json"), decodedValue)
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
    assertTrue(resp.isSuccess)
    assertEquals("accepted", resp.body)
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
    assertTrue(resp.isSuccess)
    assertEquals("accepted", resp.body)
  }
  test("Client.DoGetConnectionTimeoutFail") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-exception-client",
        delay = 0,
        retryCount = 0,
        backend = Option(testHttpBackend)
      )
    try client.doGet(endpoint = "/connect-exception")
    catch {
      case e: RuntimeException =>
        assertTrue(true)
        assertEquals(e.getMessage, "failed to complete request")
      case _: Throwable => new AssertionError
    }
  }
  test("Client.DoGetReadTimeoutFail") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-exception-client",
        delay = 0,
        retryCount = 0,
        backend = Option(testHttpBackend)
      )
    try client.doGet(endpoint = "/read-exception")
    catch {
      case e: RuntimeException =>
        assertTrue(true)
        assertEquals(e.getMessage, "failed to complete request")
      case _: Throwable => new AssertionError
    }
  }
  test("Client.DoGetTimeoutException") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-exception-client",
        delay = 0,
        retryCount = 0,
        backend = Option(testHttpBackend)
      )
    try client.doGet(endpoint = "/timeout-exception")
    catch {
      case e: RuntimeException =>
        assertTrue(true)
        assertEquals(e.getMessage, "failed to complete request")
      case _: Throwable => new AssertionError
    }
  }
  test("Client.DoGetRetryRequestOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        userAgent = "test-retry-client",
        delay = 1,
        backend = Option(testHttpBackend)
      )
    val start = currentTimeMillis()
    val resp = client.doGet(endpoint = "/retry-request")
    val requestTime = (currentTimeMillis() - start) / 1000
    assert(requestTime >= 1 && requestTime <= 2)
    assertTrue(resp.isSuccess)
    assertEquals("retry count: '2'", resp.body)
  }
}
