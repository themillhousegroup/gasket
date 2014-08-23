package com.themillhousegroup.gasket

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet.{ CellFeed, WorksheetEntry }
import com.themillhousegroup.gasket.traits.{ Timing, ScalaEntry }
import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Worksheet(private val service: SpreadsheetService, val parent: Spreadsheet, val googleEntry: WorksheetEntry) extends ScalaEntry[WorksheetEntry] with Timing {

  lazy val cellFeedBaseUrl = new URI(googleEntry.getCellFeedUrl.toString).toURL
  // + "?min-row=2&min-col=4&max-col=4").toURL();

  lazy val cellFeed = service.getFeed(cellFeedBaseUrl, classOf[CellFeed])

  /** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
  def cells: Future[Seq[Cell]] = Future {
    import scala.collection.JavaConverters._

    time("cells fetch", cellFeed.getEntries).asScala.map(Cell(this, _))
  }

  /** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
  def rows: Future[Seq[Row]] = {
    cells.map { c =>
      val cellMap = c.groupBy(_.rowNumber)

      cellMap.toSeq.map {
        case (i, cells) =>
          Row(i, cells.sorted)
      }.sorted
    }
  }
}
