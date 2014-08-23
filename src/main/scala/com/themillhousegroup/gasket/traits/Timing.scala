package com.themillhousegroup.gasket.traits

import com.google.common.base.Stopwatch
import org.slf4j.LoggerFactory

trait Timing {

  val log = LoggerFactory.getLogger(getClass)
  lazy private[this] val s = new Stopwatch()

  protected def time[T](opName: String, op: => T): T = {
    if (log.isDebugEnabled) {
      s.reset
      s.start
      val result: T = op
      s.stop
      log.debug(s"$opName took ${s.elapsedMillis}ms")
      result
    } else op
  }
}
