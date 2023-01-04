package org.vspaz.http

import org.scalatest.funsuite.AnyFunSuite
class ClientTest extends AnyFunSuite {
  test("Client.InitOk") {
    assert(
      "host: '', userAgent: '', readTimeout: '10', 'connectionTimeout: '10'" == new Client().toString
    );
  }
}
