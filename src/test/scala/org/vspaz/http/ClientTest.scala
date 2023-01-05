package org.vspaz.http

import org.scalatest.funsuite.AnyFunSuite
import sttp.client3.{Identity, Response}
import sttp.capabilities.WebSockets
import sttp.client3.testing.SttpBackendStub
import sttp.model.{MediaType, Method, StatusCode}

trait Setup {
  def getTestHttpBackendStub: SttpBackendStub[Identity, WebSockets] = SttpBackendStub
    .synchronous
    .whenRequestMatchesPartial {
      case request if (request.method.equals(Method.GET)) =>
        assert(request.uri.path.endsWith(List("test-get")))
        assert(request.headers().headers("User-Agent").head == "test-get-client")
        Response("Ok", StatusCode.Ok)
      case request if (request.method.equals(Method.DELETE)) =>
        assert(request.uri.path.endsWith(List("test-delete")))
        assert(request.headers().headers("User-Agent").head == "test-delete-client")
        Response("accepted", StatusCode.Accepted)
      case request if (request.method.equals((Method.POST))) =>
        assert(request.uri.path.endsWith(List("test-post")))
        assert(request.headers().headers("User-Agent").head == "test-client")
        Response("accepted", StatusCode.Accepted)
      case request if (request.method.equals((Method.PATCH))) =>
        assert(request.uri.path.endsWith(List("test-patch")))
        assert(request.headers().headers("User-Agent").head == "test-client")
        Response("accepted", StatusCode.Accepted)
      case request if (request.method.equals((Method.PUT))) =>
        assert(request.uri.path.endsWith(List("test-put")))
        assert(request.headers().headers("User-Agent").head == "test-client")
        Response("accepted", StatusCode.Accepted)
    }
}

class ClientTest extends AnyFunSuite with Setup {
  test("Client.InitOk") {
    assert(
      "host: '', userAgent: '', readTimeout: '10', 'connectionTimeout: '10'" == new Client()
        .toString
    );
  }
  test("Client.doGetOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        "http://mock.api",
        userAgent = "test-get-client",
        backend = Option(testHttpBackend)
      )
    client.doGet("/test-get")
  }

  test("Client.doDeleteOk") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client(
        "http://mock.api",
        userAgent = "test-delete-client",
        backend = Option(testHttpBackend)
      )
    client.doDelete("/test-delete")
  }
}
