# scala-http

an HTTP client that's simple to configure & use.
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

####  Do simple GET request

```scala
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.vspaz.http.Client

val resp = new Client().doGet(endpoint="https://httpbin.org/get")
assertTrue(resp.isOk())
assertTrue(resp.isSuccess())
assertEquals(200, resp.statusCode)
```

#### Deserialize JSON payload

```scala
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.vspaz.http.Client

case class Response(
                     args: Option[Map[String, String]],
                     headers: Option[Map[String, String]],
                     origin: Option[String],
                     url: Option[String]) {}
```

```scala
val resp = new Client().doGet(endpoint="https://httpbin.org/get")
assertTrue(resp.isOk())

val decodedBody = resp.fromJson(classOf[Response])
assertEquals("https://httpbin.org/get", decodedBody.url.get)
```

or if you need more control, you can get response as bytes or string, 
and deserialize it with any Scala/Java frameworks e.g.

```scala
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

val resp = new Client().doGet(endpoint="https://httpbin.org/get")
assertTrue(resp.isOk())

val mapper: JsonMapper = JsonMapper.builder().addModule(DefaultScalaModule).build()
val decodedBody = mapper.readValue(response.toString(), classOf[Response])
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

#### Retrying request on HTTP errors & exceptions

* `retryCount[Int]`:  number of attempts to retry the request before it fails
* `retryDelay [Int]`: incremental retry delay between requests (sec)
* `retryOnStatusCodes Set[Int]`: a set of http status codes to retry on
* `retryOnExceptions Set[Int]`: a st of exceptions to retry on
```scala
import org.vspaz.http.Client

val client = new Client(
      host= "https://httpbin.org",
      userAgent="client-name-and-version",
      retryCount = 3,
      retryDelay = 1,
      retryOnStatusCodes = Set(400, 500, 503),
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

* `basicAuthUser`: username
* `basicUserPassword`: user password

```scala
 val client = new Client(
  host= "https://httpbin.org",
  userAgent="client-name-and-version",
  basicAuthUser="user",
  basicUserPassword = "pass"
)

val resp = client.doGet(endpoint="/get")
assertTrue(resp.isOk())

```

2. Bearer token authentication

* `token`: bearer token.

```scala

val client = new Client(
  host= "https://httpbin.org",
  userAgent="client-name-and-version",
  token="iEtTTpwwPKcLNKSykKmN"
)

val resp = client.doGet(endpoint="/get")
assertTrue(resp.isOk())

// or

val resp = client.doGet(endpoint="/get", headers=Map("Authorization" -> s"Bearer iEtTTpwwPKcLNKSykKmN"))
assertTrue(resp.isOk())

```

### Timeouts

* `connectionTimeout`: connection timeout (sec)
* `readTimeout`: read timeout (sec)

```scala
val client = new Client(
  host= "https://httpbin.org",
  userAgent="client-name-and-version",
  connectionTimeout = 5,
  readTimeout = 10
)

val resp = client.doGet(endpoint="/get")
assertTrue(resp.isOk())

```

### Full Client configuration

all parameters are optional

* `host`[String]: default is an empty string
* `userAgent`[String]:
* `basicAuthUser`[String]:
* `basicUserPassword`[String]:  
* `token`[String]: 
* `retryCount`[Int]: default = 0
* `retryOnStatusCodes`[Set[Int]]: default is an empty set
* `retryOnExceptions`[Set[String]]: default is an empty set
* `retryDelay`[Int]: default = 0
* `readTimeout`[Int]: default = 10 sec
* `connectionTimeout`[Int]: default = 10 sec
* `logger`[Logger]:

```scala
import org.vspaz.http.Client

val client = new Client(
  host= "https://httpbin.org",
  userAgent="client-name-and-version",
  basicAuthUser="username",
  basicUserPassword="pass",
  token="",
  retryCount=3,
  retryDelay = 1,
  retryOnStatusCodes = Set(400, 500, 503),
  retryOnExceptions = Set(
    "java.lang.RuntimeException",
    "java.lang.Throwable"
  ),
  connectionTimeout = 5,
  readTimeout = 10
)
```

### Response object
```scala
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.vspaz.http.Client

val resp = new Client().doGet(endpoint="https://httpbin.org/get")
assertTrue(resp.isOk())
assertTrue(resp.isSuccess())
assertEquals(200, resp.statusCode)
println(resp.asString())
println(resp.asBytes())
println(resp.headers)
```