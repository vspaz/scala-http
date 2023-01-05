package org.vspaz.http

import org.scalatest.funsuite.AnyFunSuite
import sttp.client3.{Identity, Response}
import sttp.capabilities.WebSockets
import sttp.client3.testing.SttpBackendStub
import sttp.model.{Method, StatusCode}

trait Setup {
  def getTestHttpBackendStub: SttpBackendStub[Identity, WebSockets] = SttpBackendStub
    .synchronous
    .whenRequestMatchesPartial {
      case request if (request.method.equals(Method.GET)) =>
        assert(request.uri.path.endsWith(List("endpoint")))
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
  test("Client.doGet") {
    val testHttpBackend = getTestHttpBackendStub
    val client =
      new Client("http://mock.api", userAgent = "test", backend = Option(testHttpBackend))
    client.doGet("/endpoint")
  }
}
