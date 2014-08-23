package com.themillhousegroup.gasket.test

import scala.concurrent.duration.Duration
import scala.concurrent.{ Future, Await }

trait TestHelpers {
  val shortWait = Duration(100L, "millis")

  def waitFor[T](op: => Future[T]) = {
    Await.result(op, shortWait)
  }
}
