package com.themillhousegroup.gasket

import com.google.gdata.data.spreadsheet.{ WorksheetFeed, SpreadsheetEntry }
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.themillhousegroup.gasket.traits.ScalaEntry

case class Spreadsheet(private val service: SpreadsheetService, protected val entry: SpreadsheetEntry) extends ScalaEntry[SpreadsheetEntry] {

  private[this] lazy val worksheetFeed = service.getFeed(entry.getWorksheetFeedUrl(), classOf[WorksheetFeed])

  def worksheets: Seq[Worksheet] = {
    import scala.collection.JavaConverters._
    worksheetFeed.getEntries.asScala.map(Worksheet(service, this, _))
  }

}
