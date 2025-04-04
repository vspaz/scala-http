package org.vspaz.http.setup

import org.junit.jupiter.api.Assertions.assertEquals
import sttp.capabilities.WebSockets
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{Identity, Response, SttpClientException, UriContext, basicRequest}
import sttp.model.StatusCode

trait ServerErrorMockResponse {
  var retryCount: Int = 0
  var retryCountOnHttpError: Int = 0
  var retryCountOnExceptions: Int = 0

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
      case request if request.uri.path.endsWith(List("retry-request-on-http-error")) =>
        retryCountOnHttpError += 1
        if (retryCountOnHttpError == 1)
          Response(s"retry count: '$retryCountOnHttpError'", StatusCode.ServiceUnavailable)
        else if (retryCountOnHttpError == 2)
          Response(s"retry count: '$retryCountOnHttpError'", StatusCode.InternalServerError)
        else
          Response(s"retry count: '$retryCountOnHttpError'", StatusCode.Ok)
      case request if request.uri.path.endsWith(List("retry-request-on-exceptions")) =>
        retryCountOnExceptions += 1
        if (retryCountOnExceptions == 1)
          throw new SttpClientException.ConnectException(
            basicRequest.get(uri = uri"http://mock.api/retry-request-on-exceptions"),
            new RuntimeException("failed to connect")
          )
        else if (retryCountOnExceptions == 2)
          throw new SttpClientException.ReadException(
            basicRequest.get(uri = uri"http://mock.api/retry-request-on-exceptions"),
            new RuntimeException("failed to read")
          )
        else if (retryCountOnExceptions == 3)
          throw new SttpClientException.TimeoutException(
            basicRequest.get(uri = uri"http://mock.api/retry-request-on-exceptions"),
            new RuntimeException("request timed out")
          )
        else if (retryCountOnExceptions == 4)
          throw new RuntimeException("runtime error occurred")
        else if (retryCountOnExceptions == 5)
          throw new Throwable("runtime error occurred")
        else
          Response(s"retry count: '$retryCountOnExceptions'", StatusCode.Ok)
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
}
