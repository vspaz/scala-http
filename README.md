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

#### Get response headers
```scala
val resp = new Client().doGet(endpoint = "https://httpbin.org/get")
assertTrue(resp.isOk())

println(resp.headers)
assertEquals("application/json", resp.headers("content-type"))
```

### Configuring HTTP client

### Retrying request on HTTP errors & exceptions

* `retryCount[Int]`:  number of attempts to retry the request before it fails
* `retryDelay [Int]`: incremental retry delay between requests
* `retryOnErrors Set[Int]`: a set of status codes to retry on
* `retryOnExceptions Set[Int]`: a st of exceptions to retry on
```scala
import org.vspaz.http.Client

val client = new Client(
      host= "https://httpbin.org",
      userAgent="client-name-and-version",
      retryCount = 3,
      retryDelay = 1,
      retryOnErrors = Set(400, 500, 503),
      retryOnExceptions = Set(
        "java.lang.RuntimeException",
        "java.lang.Throwable"
      )
    )

val resp = client.doGet(endpoint="/get")
assertTrue(resp.isOk())

```

#### JSON serialization

1. implicit json serialization

```scala
val client = new Client(
  host= "https://httpbin.org",
  userAgent="client-name-and-version"
)

val resp = client.doPost(
  endpoint = "/post",
  headers = Map("Content-Type" -> "application/json"),
  payload = Map("key" -> "  value")
)
```

2. explicit json serialization

```scala
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

val client = new Client(
  host= "https://httpbin.org",
  userAgent="client-name-and-version"
)

val mapper: ObjectMapper = new ObjectMapper()
mapper.registerModule(DefaultScalaModule)

val resp = client.doPost(
  endpoint = "/post",
  headers = MMap("Content-Type" -> "application/json"),
  payload = mapper.writer.writeValueAsString(Map("key" -> "value"))
)
```

#### JSON deserialization

```scala
package org.vspaz


import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.vspaz.http.Client

case class Response(
                     args: Option[Map[String, String]],
                     headers: Option[Map[String, String]],
                     origin: Option[String],
                     url: Option[String]) {}

val client = new Client(
  host= "https://httpbin.org",
  userAgent="client-name-and-version"
)

val resp = client.doGet(endpoint="/get")
assertTrue(resp.isOk())
val decodedBody = resp.fromJson(classOf[Response])
```

### Authentication

1. Basic auth
```scala
 val client = new Client(
  host= "https://httpbin.org",
  userAgent="client-name-and-version",
  basicAuthUser="user",
  basicUserPassword = "pass",
  token="iEtTTpwwPKcLNKSykKmN"
)

val resp = client.doGet(endpoint="/get")
assertTrue(resp.isOk())

```
