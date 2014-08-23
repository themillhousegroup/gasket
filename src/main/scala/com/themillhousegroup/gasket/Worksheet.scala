package com.themillhousegroup.gasket

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet.{ CellFeed, WorksheetEntry }
import com.themillhousegroup.gasket.traits.{ Timing, ScalaEntry }
import java.net.URI

case class Worksheet(private val service: SpreadsheetService, val parent: Spreadsheet, val googleEntry: WorksheetEntry) extends ScalaEntry[WorksheetEntry] with Timing {

  val cellFeedBaseUrl = new URI(googleEntry.getCellFeedUrl.toString).toURL
  // + "?min-row=2&min-col=4&max-col=4").toURL();

  lazy val cellFeed = service.getFeed(cellFeedBaseUrl, classOf[CellFeed])

  /** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
  def cells: Seq[Cell] = {
    import scala.collection.JavaConverters._

    time("cells fetch", cellFeed.getEntries).asScala.map(Cell(this, _))
  }

  /** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
  def rows: Seq[Row] = {
    val cellMap = cells.groupBy(_.rowNumber)
    cellMap.toSeq.map {
      case (i, cells) =>
        Row(i, cells.sortWith { (c1, c2) => c1.colNumber < c2.colNumber })
    }.sortWith((r1, r2) => r1.rowNumber < r2.rowNumber)
  }
}
