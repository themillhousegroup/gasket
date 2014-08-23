package com.themillhousegroup.gasket

import com.google.gdata.data.spreadsheet.{ WorksheetFeed, SpreadsheetEntry }
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.themillhousegroup.gasket.traits.ScalaEntry
import com.google.common.base.Stopwatch

case class Spreadsheet(private val service: SpreadsheetService, val googleEntry: SpreadsheetEntry) extends ScalaEntry[SpreadsheetEntry] {

  private[this] lazy val worksheetFeed = service.getFeed(googleEntry.getWorksheetFeedUrl(), classOf[WorksheetFeed])

  def worksheets: Map[String, Worksheet] = {
    import scala.collection.JavaConverters._
    val s = new Stopwatch()
    s.start
    val w = worksheetFeed.getEntries.asScala.map(Worksheet(service, this, _)).map(w => w.title -> w).toMap
    s.stop
    println(s"Worksheet fetch took ${s.elapsedMillis}ms")
    w
  }

}
