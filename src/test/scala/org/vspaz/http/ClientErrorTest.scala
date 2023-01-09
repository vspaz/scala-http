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

trait ErrorMockServerResponse {
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
    }

  class ClientErrorTest extends AnyFunSuite with ErrorMockServerResponse {
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
      assertTrue(resp.isSuccess())
      assertEquals("retry count: '2'", resp.toString())
    }
  }
}