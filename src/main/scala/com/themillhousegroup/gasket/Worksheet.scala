package com.themillhousegroup.gasket

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet._
import com.themillhousegroup.gasket.traits.{ Timing, ScalaEntry }
import java.net.{ URL, URI }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.google.gdata.model.batch.BatchUtils
import com.google.gdata.data.batch.BatchOperationType
import scala.collection.JavaConverters._

case class Worksheet(val service: SpreadsheetService, val parent: Spreadsheet, val googleEntry: WorksheetEntry) extends ScalaEntry[WorksheetEntry] with Timing {

  private def toUrl(s: String): URL = new URI(s).toURL

  lazy private[this] val cellFeedBaseUrlString = googleEntry.getCellFeedUrl.toString
  lazy private[this] val cellFeedBaseUrl = toUrl(cellFeedBaseUrlString)
  lazy private[gasket] val cellFeed = service.getFeed(cellFeedBaseUrl, classOf[CellFeed])

  lazy private[this] val listFeedBaseUrlString = googleEntry.getListFeedUrl.toString
  lazy private[this] val listFeedBaseUrl = toUrl(listFeedBaseUrlString)

  lazy val rowCount = googleEntry.getRowCount
  lazy val colCount = googleEntry.getColCount

  /**
   * For convenience; The String contents of the first row of this worksheet
   */
  lazy val headerLabels: Future[Seq[String]] = {
    if (rowCount == 0) {
      Future.successful(Seq[String]())
    } else {
      val headerBlock = block(1 to 1, 1 to colCount)
      headerBlock.map(_.head.cells.map(_.value))
    }
  }

  /** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
  def cells: Future[Seq[Cell]] = Future {

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

  /**
   * When given a Seq of Cells,
   * returns a future holding a new Seq where each element is
   * a tuple of (headerLabel -> Cell)
   */
  def withHeaderLabels(cells: Seq[Cell]): Future[Seq[(String, Cell)]] = {
    headerLabels.map { headers =>
      cells.map { cell =>
        val column = cell.colNumber
        headers(column - 1) -> cell
      }
    }
  }

  /**
   * @return a Future holding the worksheet with all contents (i.e. everything except the header rows) removed.
   * The "clear" is accomplished by setting the worksheet size to "1 row high"
   * (the minimum permissible by the underlying API)
   */
  def clear: Future[Worksheet] = {
    Future {
      googleEntry.setRowCount(1)

      this.copy(googleEntry = googleEntry.update)
    }
  }

  /**
   * Adds additional rows to the bottom of the worksheet. Does not mutate the current sheet!
   * @param newRows a sequence of rows, where a row is a sequence of (headerLabel -> content) tuples
   *
   * WARNING this is NOT a batched-up operation!
   * If it is possible that you will be adding more than a couple of rows, please strongly consider
   * using the methods in Block!
   *
   *@return a Future containing the new worksheet with the added rows
   */
  def addRows(newRows: Seq[Seq[(String, String)]]): Future[Worksheet] = {
    Future {
      newRows.foreach { newRow =>
        val gRow = new ListEntry()
        newRow.foreach { cell =>
          gRow.getCustomElements.setValueLocal(cell._1, cell._2)
        }
        // Send the new row to the API for insertion. This blocks.
        service.insert(listFeedBaseUrl, gRow)
      }
    }.flatMap { _ =>
      // Finish by fetching "this sheet" from the remote end again:
      refreshFromRemote
    }
  }

  private def refreshFromRemote: Future[Worksheet] = {
    parent.worksheets.map { sheetMap =>
      sheetMap(title)
    }
  }

  /**
   * Adds additional rows to the bottom of the worksheet. Does not mutate the current sheet!
   *
   * Each row you submit must have EXACTLY the same number of items as headerLabels!
   *
   * @param newRows a sequence of rows, where each row is a sequence of String values.
   *
   * @return a Future containing the new worksheet with the added rows
   */
  def addFullRows(newRows: Seq[Seq[String]]): Future[Worksheet] = {

    headerLabels.flatMap { labels =>
      val expectedSize = labels.size

      // Check precondition: length of each row == expected:
      val incorrectlySizedRows = newRows.filterNot(_.size == expectedSize)

      if (!incorrectlySizedRows.isEmpty) {
        Future.failed(new IllegalArgumentException(s"Rows: $incorrectlySizedRows were not of expected length $expectedSize"))
      } else {
        val zipped = newRows.map { newRow =>
          labels.zip(newRow)
        }

        addRows(zipped)
      }
    }
  }
}
