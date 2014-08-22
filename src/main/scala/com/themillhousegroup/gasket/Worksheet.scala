package com.themillhousegroup.gasket

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet.{ ListFeed, WorksheetEntry }
import com.themillhousegroup.gasket.traits.ScalaEntry

case class Worksheet(private val service: SpreadsheetService, val parent: Spreadsheet, protected val entry: WorksheetEntry) extends ScalaEntry[WorksheetEntry] {

  private[this] lazy val rowFeed = service.getFeed(entry.getListFeedUrl(), classOf[ListFeed])

  def rows: Seq[Row] = {
    import scala.collection.JavaConverters._
    rowFeed.getEntries.asScala.map(Row(this, _))
  }
}
