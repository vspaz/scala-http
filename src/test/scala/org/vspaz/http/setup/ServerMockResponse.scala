package org.vspaz.http.setup

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import sttp.capabilities.WebSockets
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{Identity, Response, StringBody}
import sttp.model.{Header, MediaType, Method, StatusCode}

import scala.collection.immutable.Seq

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
        assertTrue(request.uri.path.endsWith(List("test-get-method")))
        assertEquals("test-get-client", request.headers().headers("User-Agent").head)
        Response(
          body = "Ok",
          code = StatusCode.Ok,
          statusText = "Ok",
          headers = Seq(new Header("test", "get"))
        )
      case request if request.method.equals(Method.HEAD) =>
        assertTrue(request.uri.path.endsWith(List("test-head-method")))
        assertEquals("test-head-client", request.headers().headers("User-Agent").head)
        Response("Ok", StatusCode.Ok)
      case request if request.method.equals(Method.DELETE) =>
        assertTrue(request.uri.path.endsWith(List("test-delete-method")))
        assertEquals("test-delete-client", request.headers().headers("User-Agent").head)
        Response("accepted", StatusCode.Accepted)
      case request if request.method.equals(Method.POST) =>
        assertTrue(request.uri.path.endsWith(List("test-post-method")))
        assertEquals("test-post-client", request.headers().headers("User-Agent").head)
        assertEquals(
          MediaType.ApplicationJson.toString(),
          request.headers().headers("Content-Type").head
        )
        assertEquals(
          StringBody("""{"test":"json_post_method"}""", "utf-8", MediaType.TextPlain),
          request.body
        )
        Response(
          body = serializer.writer.writeValueAsString(Map("test" -> "json_post_method")),
          code = StatusCode.Accepted,
          statusText = "accepted",
          headers = Seq(new Header("foo", "bar"))
        )
      case request if request.method.equals(Method.PATCH) =>
        assertTrue(request.uri.path.endsWith(List("test-patch-method")))
        assertEquals("test-patch-client", request.headers().headers("User-Agent").head)
        assertEquals(
          MediaType.ApplicationJson.toString(),
          request.headers().headers("Content-Type").head
        )
        assertEquals(
          StringBody("""{"test":"json_patch_method"}""", "utf-8", MediaType.TextPlain),
          request.body
        )
        Response(
          serializer.writer.writeValueAsString(Map("test" -> "json_patch_method")),
          StatusCode.Accepted
        )
      case request if request.method.equals(Method.PUT) =>
        assertTrue(request.uri.path.endsWith(List("test-put-method")))
        assertEquals("test-put-client", request.headers().headers("User-Agent").head)
        assertEquals(
          MediaType.ApplicationJson.toString(),
          request.headers().headers("Content-Type").head
        )
        assertEquals(
          StringBody("""{"test":"json_put_method"}""", "utf-8", MediaType.TextPlain),
          request.body
        )
        Response(
          serializer.writer.writeValueAsString(Map("test" -> "json_put_method")),
          StatusCode.Accepted
        )
    }
}
