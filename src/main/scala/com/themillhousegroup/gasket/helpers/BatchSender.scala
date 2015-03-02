package com.themillhousegroup.gasket.helpers

import com.google.gdata.data.spreadsheet.{ CellEntry, CellFeed }
import scala.concurrent.Future
import com.themillhousegroup.gasket.{ Worksheet, Cell }
import java.net.URL
import com.google.gdata.data.batch.{ BatchOperationType, BatchUtils }
import scala.collection.JavaConverters._
import com.google.gdata.data.ILink._
import scala.concurrent.ExecutionContext.Implicits.global
import com.themillhousegroup.gasket.traits.Timing

trait BatchSender extends Timing {

  val worksheet: Worksheet

  val batchLink = worksheet.cellFeed.getLink(Rel.FEED_BATCH, Type.ATOM)

  private def sendBatchRequest(batchRequest: CellFeed): Future[Seq[Cell]] = {
    Future(time("Batch operation", worksheet.service.batch(new URL(batchLink.getHref), batchRequest))).map { batchResponseCellFeed =>
      val responseEntries = batchResponseCellFeed.getEntries.asScala
      val failures = responseEntries.filter(!BatchUtils.isSuccess(_)).map(BatchUtils.getBatchStatus(_))

      if (failures.isEmpty) {
        responseEntries.map(Cell(worksheet, _))
      } else {
        throw new IllegalStateException(s"Batch request failed: ${failures.head.getReason}")
      }
    }
  }

  private def buildBatchEntry(cell: Cell, newValue: String, op: BatchOperationType): CellEntry = {
    val batchEntry = new CellEntry(cell.googleEntry)
    batchEntry.setId(s"${worksheet.cellFeedBaseUrlString}/${cell.idString}")
    batchEntry.changeInputValueLocal(newValue)
    BatchUtils.setBatchId(batchEntry, cell.idString)
    BatchUtils.setBatchOperationType(batchEntry, op)
    batchEntry
  }

  private def buildBatchRequest(cellsWithTheirNewValues: Seq[(Cell, String)], op: BatchOperationType): CellFeed = {
    val batchRequestCellFeed = new CellFeed()

    cellsWithTheirNewValues.foreach {
      case (cell, newValue) =>
        val batchEntry = buildBatchEntry(cell, newValue, op)
        batchRequestCellFeed.getEntries.add(batchEntry)
    }
    batchRequestCellFeed
  }

  private def buildBatchUpdateRequest(cellsWithTheirNewValues: Seq[(Cell, String)]): CellFeed = {
    buildBatchRequest(cellsWithTheirNewValues, BatchOperationType.UPDATE)

  }

  def sendBatchUpdate(cells: Seq[Cell], newValues: Seq[String]): Future[Seq[Cell]] = {
    val cellsWithTheirNewValues = cells.zip(newValues)
    val batchRequest = buildBatchUpdateRequest(cellsWithTheirNewValues)
    sendBatchRequest(batchRequest)
  }
}
