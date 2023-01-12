package org.vspaz.http

import java.lang.System.currentTimeMillis
import org.scalatest.funsuite.AnyFunSuite
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.vspaz.http.setup.ServerErrorMockResponse

class ClientErrorTest extends AnyFunSuite with ServerErrorMockResponse {
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
          retryOnExceptions = Set("sttp.client3.SttpClientException.ReadException"),
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
  test("Client.DoGetRetryRequestOnHttpErrorOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        delay = 1,
        retryOnErrors = Set(503, 500),
        backend = Option(testHttpBackend)
      )
    val start = currentTimeMillis()
    val resp = client.doGet(endpoint = "/retry-request-on-http-error")
    val requestTime = (currentTimeMillis() - start) / 1000
    assert(requestTime >= 1 && requestTime <= 4)
    assertTrue(resp.isSuccess())
    assertEquals("retry count: '3'", resp.toString())
  }
  test("Client.DoGetRetryRequestOnExceptionsErrorOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        host = "http://mock.api",
        retryCount = 6,
        delay = 1,
        retryOnExceptions = Set(
          "sttp.client3.SttpClientException.ConnectException",
          "sttp.client3.SttpClientException.ReadException",
          "sttp.client3.SttpClientException.TimeoutException",
          "java.lang.RuntimeException",
          "java.lang.Throwable",
        ),
        backend = Option(testHttpBackend)
      )
    val resp = client.doGet(endpoint = "/retry-request-on-exceptions")
    assertTrue(resp.isSuccess())
    assertEquals("retry count: '6'", resp.toString())
  }
}