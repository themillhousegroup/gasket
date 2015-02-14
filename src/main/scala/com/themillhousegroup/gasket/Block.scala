package com.themillhousegroup.gasket

import scala.concurrent.Future
import com.google.gdata.data.Link
import java.net.URL
import com.google.gdata.data.spreadsheet.{ CellEntry, CellFeed }
import com.google.gdata.data.batch.{ BatchOperationType, BatchUtils }

/**
 * Represents a rectangular area of a Worksheet that has already
 * been fetched from the remote API.
 *
 * Operations on a Block will be performed using the Google API's
 * batching mechanism for improved performance over single-Cell updates.
 *
 *
 */
case class Block(parent: Worksheet, cells: Seq[Cell]) extends Ordered[Block] {

  lazy val minRow = cells.head.rowNumber
  lazy val minColumn = cells.head.colNumber

  lazy val width = cells.last.colNumber - minColumn
  lazy val height = cells.last.rowNumber - minRow

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
      // TODO: Update the remote sheet using the Batch API
      Future.successful(this)
    }
  }

  def rows: Seq[Row] = {
    cells.grouped(width).map { cRow =>
      Row(cRow.head.rowNumber, cRow)
    }.toSeq
  }

  private object BatchSender {

    import com.google.gdata.data.ILink._
    lazy val batchLink = parent.cellFeed.getLink(Rel.FEED_BATCH, Type.ATOM)

    private def sendBatchRequest(batchRequest: CellFeed) = {
      val batchResponseCellFeed = parent.service.batch(new URL(batchLink.getHref), batchRequest)
      Future.successful(Seq[Cell]())
    }

    private def buildBatchUpdateRequest(cellsWithTheirNewValues: Seq[(Cell, String)]): CellFeed = {
      val batchRequestCellFeed = new CellFeed()

      cellsWithTheirNewValues.foreach {
        case (cell, newValue) =>
          val batchEntry = new CellEntry()
          // TODO continue ripping from https://developers.google.com/google-apps/spreadsheets/#updating_multiple_cells_with_a_batch_request
          // Work out equivalent for: new CellEntry(cellEntries.get(cell.idString))
          batchEntry.changeInputValueLocal(newValue)
          BatchUtils.setBatchId(batchEntry, cell.idString)
          BatchUtils.setBatchOperationType(batchEntry, BatchOperationType.UPDATE)
          batchRequestCellFeed.getEntries().add(batchEntry)
      }
      batchRequestCellFeed
    }

    def sendBatchUpdate(cells: Seq[Cell], newValues: Seq[String]): Future[Seq[Cell]] = {
      val cellsWithTheirNewValues = cells.zip(newValues)
      val batchRequest = buildBatchUpdateRequest(cellsWithTheirNewValues)
      sendBatchRequest(batchRequest)
    }
  }
}
