package com.themillhousegroup.gasket.traits

import java.util.concurrent.TimeUnit

import com.google.common.base.Stopwatch
import org.slf4j.LoggerFactory

trait Timing {

  val log = LoggerFactory.getLogger(getClass)

  protected def time[T](opName: String, op: => T): T = {
    if (log.isDebugEnabled) {
      val s = Stopwatch.createUnstarted()
      s.reset
      s.start
      val result: T = op
      s.stop
      log.debug(s"$opName took ${s.elapsed(TimeUnit.MILLISECONDS)}ms")
      result
    } else op
  }
}
