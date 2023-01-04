package org.vspaz.http

class Client(
    host: String = "",
    userAgent: String = "",
    basicAuthUser: String = "",
    basicUserPassword: String = "",
    retryCount: Int = 3,
    retryOnErrors: List[Int] = List(),
    delay: Int = 2,
    readTimeout: Int = 10,
    connectionTimeout: Int = 10
) {}
