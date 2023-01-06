package org.vspaz.http

import scala.concurrent.duration.{Duration, SECONDS}
import sttp.client3.{Identity, _}
import sttp.model.Method

import System.currentTimeMillis

class Client(
  host: String = "",
  userAgent: String = "",
  basicAuthUser: String = "",
  basicUserPassword: String = "",
  token: String = "",
  retryCount: Int = 3,
  retryOnErrors: Set[Int] = Set(),
  delay: Int = 2,
  readTimeout: Int = 10,
  connectionTimeout: Int = 10,
  logger: Option[ILogger] = None,
  backend: Option[SttpBackend[Identity, Any]] = None
) {
  private val responseTimeout = Duration(readTimeout, SECONDS)
  private val http = backend.getOrElse(
    HttpClientSyncBackend(options =
      SttpBackendOptions.connectionTimeout(Duration(connectionTimeout, SECONDS))
    )
  )
  private def buildRequest(
    method: Method,
    endpoint: String,
    headers: Map[String, String] = Map(),
    payload: String = ""
  ): RequestT[Identity, String, Any] = {
    val request = basicRequest
      .headers(headers ++ Map("User-Agent" -> userAgent))
      .readTimeout(responseTimeout)
      .method(method, uri = uri"${host + endpoint}")
      .body(payload)
      .response(asStringAlways)
    if (basicAuthUser != "" && basicUserPassword != "")
      request.auth.basic(basicAuthUser, basicUserPassword)
    request
  }

  private def handleRequest(
    method: Method,
    endpoint: String,
    headers: Map[String, String],
    payload: String
  ): Response[String] = {
    var response: Identity[Response[String]] = null
    try
      response = buildRequest(
        method = method,
        endpoint = endpoint,
        headers = headers,
        payload = payload
      ).send(http)

    catch {
      case e: sttp.client3.SttpClientException.ConnectException =>
        println(s"${e.getCause} occurred")
      case e: sttp.client3.SttpClientException.ReadException => println(s"${e.getCause} occurred")
      case e: Exception                                      => println(s"${e.getCause} occurred")
    }
    response
  }

  private def doRequest(
    method: Method = Method.GET,
    endpoint: String,
    headers: Map[String, String],
    payload: String = ""
  ): Identity[Response[String]] = timeIt(
    handleRequest(method = method, endpoint = endpoint, headers = headers, payload = payload)
  )

  def doGet(endpoint: String, headers: Map[String, String] = Map()): Identity[Response[String]] =
    doRequest(method = Method.GET, endpoint = endpoint, headers = headers)

  def doPost(
    endpoint: String,
    headers: Map[String, String] = Map(),
    payload: String = ""
  ): Identity[Response[String]] = doRequest(
    method = Method.POST,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  )

  def doPatch(
    endpoint: String,
    headers: Map[String, String] = Map(),
    payload: String = ""
  ): Identity[Response[String]] = doRequest(
    method = Method.PATCH,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  )

  def doPut(
    endpoint: String,
    headers: Map[String, String] = Map(),
    payload: String = ""
  ): Identity[Response[String]] = doRequest(
    method = Method.PUT,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  )

  def doDelete(endpoint: String, headers: Map[String, String] = Map()): Identity[Response[String]] =
    doRequest(method = Method.DELETE, endpoint = endpoint, headers = headers)

  private def timeIt[T](expression: => T): T = {
    val start = currentTimeMillis()
    val result = expression
    println(s"request took {$currentTimeMillis() - $start} to complete")
    result
  }

  override def toString: String =
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
}
