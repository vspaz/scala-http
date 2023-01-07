package org.vspaz.http

import scala.concurrent.duration.{Duration, SECONDS}
import sttp.client3.{Identity, _}
import sttp.model.Method
import org.slf4j.{Logger, LoggerFactory}

import System.currentTimeMillis
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

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
  backend: Option[SttpBackend[Identity, Any]] = None
) {
  private val responseTimeout = Duration(readTimeout, SECONDS)
  private val http = backend.getOrElse(
    HttpClientSyncBackend(options =
      SttpBackendOptions.connectionTimeout(Duration(connectionTimeout, SECONDS))
    )
  )
  private val logger: Logger = LoggerFactory.getLogger(getClass.getName)
  private val mapper: ObjectMapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

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
    payload: Object
  ): Response[String] = {
    var response: Identity[Response[String]] = null
    try
      response = buildRequest(
        method = method,
        endpoint = endpoint,
        headers = headers,
        payload = mapper.writer.writeValueAsString(payload)
      ).send(http)
    catch {
      case e: sttp.client3.SttpClientException.ConnectException =>
        logger.error(s"${e.getCause} occurred")
      case e: sttp.client3.SttpClientException.ReadException =>
        logger.error(s"${e.getCause} occurred")
      case e: Exception => logger.error(s"${e.getCause} occurred")
    }
    response
  }

  private def retryRequest(
    method: Method,
    endpoint: String,
    headers: Map[String, String],
    payload: Object
  ): Response[String] = {
    for (attemptCount <- 1 to retryCount) {
      val response: Identity[Response[String]] = timeIt(
        handleRequest(method = method, endpoint = endpoint, headers = headers, payload = payload)
      )
      if (response != null) {
        if (response.code.isSuccess)
          return response
        if (!retryOnErrors.contains(response.code.code))
          throw new RuntimeException(s"can't retry on {${response.code.code}}")
      }
      Thread.sleep(delay * attemptCount)
    }
    throw new RuntimeException("failed to complete request")
  }

  private def doRequest(
    method: Method = Method.GET,
    endpoint: String,
    headers: Map[String, String],
    payload: Object = ""
  ): Identity[Response[String]] = retryRequest(
    method = method,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  )

  def doGet(endpoint: String, headers: Map[String, String] = Map()): Identity[Response[String]] =
    doRequest(method = Method.GET, endpoint = endpoint, headers = headers)

  def doPost(
    endpoint: String,
    headers: Map[String, String] = Map(),
    payload: Object = None
  ): Identity[Response[String]] = doRequest(
    method = Method.POST,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  )

  def doPatch(
    endpoint: String,
    headers: Map[String, String] = Map(),
    payload: Object = None
  ): Identity[Response[String]] = doRequest(
    method = Method.PATCH,
    endpoint = endpoint,
    headers = headers,
    payload = payload
  )

  def doPut(
    endpoint: String,
    headers: Map[String, String] = Map(),
    payload: Object = None
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
    logger.info(s"request took {$currentTimeMillis() - $start} to complete")
    result
  }

  override def toString: String =
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
}
