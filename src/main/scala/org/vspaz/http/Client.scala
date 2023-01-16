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
  token: Option[String] = None,
  retryCount: Int = 3,
  retryOnStatusCodes: Set[Int] = Set(),
  retryOnExceptions: Set[String] = Set(),
  retryDelay: Int = 2,
  readTimeout: Int = 10,
  connectionTimeout: Int = 10,
  logger: Logger = LoggerFactory.getLogger("client"),
  backend: Option[SttpBackend[Identity, Any]] = None
) extends Serializable {
  private val responseTimeout = Duration(readTimeout, SECONDS)
  private val http = backend.getOrElse(
    HttpClientSyncBackend(options =
      SttpBackendOptions.connectionTimeout(Duration(connectionTimeout, SECONDS))
    )
  )
  private val mapper: ObjectMapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  private def buildRequest(
    method: Method,
    endpoint: String,
    headers: Map[String, String] = Map(),
    params: Map[String, String] = Map(),
    payload: Option[Any] = None
  ): RequestT[Identity, String, Any] = {

    var queryParams: List[String] = List[String]()
    if (params.nonEmpty)
      queryParams =
        (for ((param, value) <- params)
          yield s"$param=$value").toList
    var queryParamString = ""
    if (queryParams.nonEmpty)
      queryParamString = "?" + queryParams.mkString("&")

    var request = basicRequest
      .headers(headers ++ Map("User-Agent" -> userAgent))
      .readTimeout(responseTimeout)
      .method(method, uri = uri"${host + endpoint + queryParamString}")
    if (basicAuthUser != "" && basicUserPassword != "")
      request = request.auth.basic(basicAuthUser, basicUserPassword)
    if (token.isDefined)
      request = request.auth.bearer(token.get)
    if (payload.isDefined)
      if (payload.get.isInstanceOf[String])
        request = request.body(payload.get.toString)
      else
        request = request.body(mapper.writer.writeValueAsString(payload.get))
    request.response(asStringAlways)
  }

  private def logError(exception: Exception): Unit = logger
    .error(s"${exception.getCause}: ${exception.getMessage}")

  private def raiseOnNonRetriableException(e: Throwable): Unit =
    if (!retryOnExceptions.contains(e.getClass.getCanonicalName))
      throw e

  private def handleRequest(
    method: Method,
    endpoint: String,
    headers: Map[String, String],
    params: Map[String, String],
    payload: Any = None
  ): Option[Response[String]] = {
    var response: Option[Identity[Response[String]]] = None
    try
      response = Option(
        buildRequest(
          method = method,
          endpoint = endpoint,
          headers = headers,
          params = params,
          payload = Option(payload)
        ).send(http)
      )
    catch {
      case e: sttp.client3.SttpClientException.ConnectException =>
        logError(exception = e)
        raiseOnNonRetriableException(e)
      case e: sttp.client3.SttpClientException.ReadException =>
        logError(exception = e)
        raiseOnNonRetriableException(e)
      case e: sttp.client3.SttpClientException.TimeoutException =>
        logError(exception = e)
        raiseOnNonRetriableException(e)
      case e: RuntimeException =>
        logError(exception = e)
        raiseOnNonRetriableException(e)
      case e: Throwable =>
        logger.error(s"${e.getCause}: ${e.getMessage}")
        raiseOnNonRetriableException(e)
    }
    response
  }

  private def retryRequest(
    method: Method,
    endpoint: String,
    headers: Map[String, String],
    params: Map[String, String],
    payload: Any
  ): ResponseWrapper = {
    for (attemptCount <- 1 to retryCount) {
      val response: Option[Identity[Response[String]]] = timeIt(
        handleRequest(
          method = method,
          endpoint = endpoint,
          headers = headers,
          params = params,
          payload = payload
        )
      )
      if (response.isDefined) {
        if (response.get.isSuccess)
          return new ResponseWrapper(response.get)
        if (!retryOnStatusCodes.contains(response.get.code.code))
          throw new RuntimeException(s"can't retry on {${response.get.code.code}}")
      }
      Thread.sleep(retryDelay * 1000 * attemptCount)
    }
    throw new RuntimeException("failed to complete request")
  }

  private def doRequest(
    method: Method = Method.GET,
    endpoint: String,
    headers: Map[String, String],
    params: Map[String, String],
    payload: Any = None
  ): ResponseWrapper = retryRequest(
    method = method,
    endpoint = endpoint,
    headers = headers,
    params = params,
    payload = payload
  )

  def doGet(
    endpoint: String,
    headers: Map[String, String] = Map(),
    params: Map[String, String] = Map()
  ): ResponseWrapper = doRequest(
    method = Method.GET,
    endpoint = endpoint,
    headers = headers,
    params = params
  )

  def doHead(
    endpoint: String,
    headers: Map[String, String] = Map(),
    params: Map[String, String] = Map()
  ): ResponseWrapper = doRequest(
    method = Method.HEAD,
    endpoint = endpoint,
    headers = headers,
    params = params
  )

  def doTrace(
    endpoint: String,
    headers: Map[String, String] = Map(),
    params: Map[String, String] = Map()
  ): ResponseWrapper = doRequest(
    method = Method.TRACE,
    endpoint = endpoint,
    headers = headers,
    params = params
  )

  def doPost(
    endpoint: String,
    headers: Map[String, String] = Map(),
    params: Map[String, String] = Map(),
    payload: Any = None
  ): ResponseWrapper = doRequest(
    method = Method.POST,
    endpoint = endpoint,
    headers = headers,
    params = params,
    payload = payload
  )

  def doPatch(
    endpoint: String,
    headers: Map[String, String] = Map(),
    params: Map[String, String] = Map(),
    payload: Any = None
  ): ResponseWrapper = doRequest(
    method = Method.PATCH,
    endpoint = endpoint,
    params = params,
    headers = headers,
    payload = payload
  )

  def doPut(
    endpoint: String,
    headers: Map[String, String] = Map(),
    params: Map[String, String] = Map(),
    payload: Any = None
  ): ResponseWrapper = doRequest(
    method = Method.PUT,
    endpoint = endpoint,
    headers = headers,
    params = params,
    payload = payload
  )

  def doDelete(
    endpoint: String,
    headers: Map[String, String] = Map(),
    params: Map[String, String] = Map()
  ): ResponseWrapper = doRequest(
    method = Method.DELETE,
    endpoint = endpoint,
    headers = headers,
    params = params
  )

  private def timeIt[T](expression: => T): T = {
    val start = currentTimeMillis()
    val result = expression
    logger.info(s"request took {$currentTimeMillis() - $start} to complete")
    result
  }

  override def toString: String =
    s"host: '$host', userAgent: '$userAgent', readTimeout: '$readTimeout', 'connectionTimeout: '$connectionTimeout'"
}
