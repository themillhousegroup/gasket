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
import com.themillhousegroup.gasket.helpers.BatchSender

case class Worksheet(val service: SpreadsheetService, val parent: Spreadsheet, val googleEntry: WorksheetEntry) extends ScalaEntry[WorksheetEntry] with BatchSender with Timing {

  val worksheet = this
  private def toUrl(s: String): URL = new URI(s).toURL

  lazy private[gasket] val cellFeedBaseUrlString = googleEntry.getCellFeedUrl.toString
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
   * Adds a single row to the bottom of the worksheet. Does NOT mutate the current Worksheet object!
   * @param newRow a sequence of string values representing the cell contents of the new row
   *
   * WARNING this is NOT a batched-up operation, and it's not particularly fast.
   * If you have multiple rows to add, rather than calling this method in some kind of loop,
   * please consider using addRows() which uses a batch mechanism to improve update performance.
   *
   * @return a Future containing the new worksheet with the added row
   */
  def addRow(newRow: Seq[String]): Future[Worksheet] = {
    headerLabels.flatMap { labels =>
      if (newRow.size != labels.size) {
        Future.failed(throw new IllegalArgumentException(s"Row: $newRow was not of expected length ${labels.size}"))
      } else {
        val zippedRow = labels.zip(newRow)

        val gRow = new ListEntry()
        zippedRow.foreach { cell =>
          gRow.getCustomElements.setValueLocal(cell._1, cell._2)
        }
        // Send the new row to the API for insertion. This blocks.
        Future(service.insert(listFeedBaseUrl, gRow))
      }
    }.flatMap { _ =>
      // Finish by fetching "this sheet" from the remote end again:
      refreshFromRemote
    }
  }

  /**
   * Adds additional rows to the bottom of the worksheet. Does NOT mutate the current Worksheet object!
   *
   * For performance, this is a batched operation. The "official" way to add rows is one-at-a-time
   * which gives unacceptable performance for anything more than a couple of rows.
   *
   * @return a Future containing the new worksheet with the added rows
   */
  def addRows(newRows: Seq[Seq[String]]): Future[Worksheet] = {
    def cellsInNewArea(previousMaxRow: Int, numRowsNeeded: Int, numColsNeeded: Int) = {
      val rowRange = (previousMaxRow + 1) to (previousMaxRow + numRowsNeeded)

      rowRange.toSeq.flatMap { rowNum =>
        (1 to numColsNeeded).map { colNum =>
          val ce = new CellEntry(rowNum, colNum, "")
          println(s"Built cell entry at $rowNum, $colNum")
          new Cell(this, ce)
        }
      }
    }

    cells.flatMap { cls =>

      val previousMaxRow = cls.lastOption.map(_.rowNumber).getOrElse(1)
      val newRowsNeeded = newRows.size
      val maxCol = cls.lastOption.map(_.colNumber).getOrElse(1)

      println(s"Need ${previousMaxRow + newRowsNeeded} total rows")

      // Expand the remote sheet
      googleEntry.setRowCount(previousMaxRow + newRowsNeeded)
      val largerSheet = copy(googleEntry = googleEntry.update)

      val newCells = cellsInNewArea(previousMaxRow, newRowsNeeded, maxCol)
      sendBatchUpdate(largerSheet, newCells, newRows.flatten).map { _ =>
        largerSheet
      }
    }.flatMap { s =>
      s.refreshFromRemote
    }
  }

  private[gasket] def refreshFromRemote: Future[Worksheet] = {
    parent.worksheets.map { sheetMap =>
      sheetMap(title)
    }
  }

  //  /**
  //   * Adds additional rows to the bottom of the worksheet. Does not mutate the current Worksheet object!
  //   *
  //   * Each row you submit must have EXACTLY the same number of items as headerLabels!
  //   *
  //   * @param newRows a sequence of rows, where each row is a sequence of String values.
  //   *
  //   * @return a Future containing the new worksheet with the added rows
  //   */
  //  def addFullRows(newRows: Seq[Seq[String]]): Future[Worksheet] = {
  //
  //    headerLabels.flatMap { labels =>
  //      val expectedSize = labels.size
  //
  //      // Check precondition: length of each row == expected:
  //      val incorrectlySizedRows = newRows.filterNot(_.size == expectedSize)
  //
  //      if (!incorrectlySizedRows.isEmpty) {
  //        Future.failed(new IllegalArgumentException(s"Rows: $incorrectlySizedRows were not of expected length $expectedSize"))
  //      } else {
  //        val zipped = newRows.map { newRow =>
  //          labels.zip(newRow)
  //        }
  //
  //        addRows(zipped)
  //      }
  //    }
  //  }
}
