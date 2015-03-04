package com.themillhousegroup.gasket

import scala.concurrent.Future
import com.google.gdata.data.Link
import java.net.URL
import com.google.gdata.data.spreadsheet.{ CellEntry, CellFeed }
import com.google.gdata.data.batch.{ BatchOperationType, BatchUtils }
import scala.concurrent.ExecutionContext.Implicits.global
import com.themillhousegroup.gasket.helpers.BatchSender

/**
 * Represents a rectangular area of a Worksheet that has already
 * been fetched from the remote API.
 *
 * Operations on a Block will be performed using the Google API's
 * batching mechanism for improved performance over single-Cell updates.
 *
 *
 */
case class Block(val worksheet: Worksheet, cells: Seq[Cell]) extends Ordered[Block] {

  lazy val batchSender = new BatchSender(worksheet)
  lazy val minRow = cells.head.rowNumber
  lazy val minColumn = cells.head.colNumber

  lazy val width = (cells.last.colNumber - minColumn) + 1
  lazy val height = (cells.last.rowNumber - minRow) + 1

  def compare(that: Block): Int = this.minRow - that.minRow

  /**
   * A new value must be provided for each cell in the block,
   * even if it's unchanged. The length of `newValues` must be
   * the same as the original `cells` sequence.
   */
  def update(newValues: Seq[String]): Future[Block] = {
    if (newValues.size != cells.size) {
      Future.failed(new IllegalArgumentException(s"Expected ${cells.size} new values, but was given ${newValues.size}"))
    } else {
      batchSender.sendBatchUpdate(cells, newValues).map { updatedCells =>
        this.copy(cells = updatedCells)
      }
    }
  }

  def rows: Seq[Row] = {
    cells.grouped(width).map { cRow =>
      Row(cRow.head.rowNumber, cRow)
    }.toSeq
  }

}
