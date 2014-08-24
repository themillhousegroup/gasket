package com.themillhousegroup.gasket

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet.{ CellFeed, WorksheetEntry }
import com.themillhousegroup.gasket.traits.{ Timing, ScalaEntry }
import java.net.{ URL, URI }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Worksheet(private val service: SpreadsheetService, val parent: Spreadsheet, val googleEntry: WorksheetEntry) extends ScalaEntry[WorksheetEntry] with Timing {

  private def toUrl(s: String): URL = new URI(s).toURL

  lazy val cellFeedBaseUrlString = googleEntry.getCellFeedUrl.toString
  lazy val cellFeedBaseUrl = toUrl(cellFeedBaseUrlString)
  lazy val cellFeed = service.getFeed(cellFeedBaseUrl, classOf[CellFeed])

  /** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
  def cells: Future[Seq[Cell]] = Future {
    import scala.collection.JavaConverters._

    time("cells fetch", cellFeed.getEntries).asScala.map(Cell(this, _))
  }

  private def asRows(cells: Future[Seq[Cell]]): Future[Seq[Row]] = {
    cells.map { c =>
      val cellMap = c.groupBy(_.rowNumber)

      cellMap.toSeq.map {
        case (i, cells) =>
          Row(i, cells.sorted)
      }.sorted
    }
  }

  /** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
  def rows: Future[Seq[Row]] = asRows(cells)

  /**
   * Returns a rectangular block of cells,
   * arranged as a sequence of Rows
   */
  def block(rowNumbers: Range, colNumbers: Range): Future[Seq[Row]] = {
    val blockCellFeedUrl = toUrl(cellFeedBaseUrlString +
      s"?min-row=${rowNumbers.head}&max-row=${rowNumbers.last}" +
      s"&min-col=${colNumbers.head}&max-col=${colNumbers.last}")

    val blockCellFeed = service.getFeed(blockCellFeedUrl, classOf[CellFeed])

    import scala.collection.JavaConverters._
    val futureBlockCells = Future(time("cell block fetch", blockCellFeed.getEntries).asScala.map(Cell(this, _)))

    asRows(futureBlockCells)
  }
}
