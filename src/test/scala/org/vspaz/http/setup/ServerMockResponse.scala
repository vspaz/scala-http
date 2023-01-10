package org.vspaz.http.setup

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import sttp.capabilities.WebSockets
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{Identity, Response, StringBody}
import sttp.model.{MediaType, Method, StatusCode}

trait ServerMockResponse {
  var retryCount: Int = 0

  val serializer: JsonMapper = JsonMapper.builder().build()
  serializer.registerModule(DefaultScalaModule)

  val deserializer: ObjectMapper = new ObjectMapper()
  deserializer.registerModule(DefaultScalaModule)

  def getTestHttpBackendStub: SttpBackendStub[Identity, WebSockets] = SttpBackendStub
    .synchronous
    .whenRequestMatchesPartial {
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