# scala-http

a simple HTTP client that's simple to configure & use.
it provides a simple configuration for:
- request retries on specific HTTP errors & exceptions
- timeouts (connection, read, idle etc.)
- basic & token auth
- json serialization & deserialization out of the box
- extra helpers
- logging
etc.

### How-to:

#### Use the client w/o any configuration

```scala
package org.vspaz


import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.vspaz.http.Client

case class Response(
                     args: Option[Map[String, String]],
                     headers: Option[Map[String, String]],
                     origin: Option[String],
                     url: Option[String]) {}
```

####  Do simple GET request

```scala
val resp = new Client().doGet(endpoint="https://httpbin.org/get")
assertTrue(resp.isOk())
assertTrue(resp.isSuccess())
assertEquals(200, resp.statusCode)
```

#### Deserialize JSON payload

```scala
val resp = new Client().doGet(endpoint="https://httpbin.org/get")
assertTrue(resp.isOk())

val decodedBody = resp.fromJson(classOf[Response])
assertEquals("https://httpbin.org/get", decodedBody.url.get)
```

#### Add query params to request

```scala
val resp = new Client().doGet(endpoint = "https://httpbin.org/get", params = Map("foo" -> "bar"))
assertTrue(resp.isOk())
    
val decodedBody = resp.fromJson(classOf[Response])
assertEquals("https://httpbin.org/get?foo=bar", decodedBody.url.get)
assertEquals("bar", decodedBody.args.get("foo"))
```

#### Add headers to request

```scala
val resp = new Client().doGet(endpoint = "https://httpbin.org/get", headers = Map("Header-Type" -> "header-value"))
assertTrue(resp.isOk())

val decodedBody = resp.fromJson(classOf[Response])
assertEquals("header-value", decodedBody.headers.get("Header-Type"))
```
