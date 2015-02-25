package com.themillhousegroup.gasket.helpers

import com.google.gdata.data.spreadsheet.{ CellEntry, CellFeed }
import scala.concurrent.Future
import com.themillhousegroup.gasket.{ Worksheet, Cell }
import java.net.URL
import com.google.gdata.data.batch.{ BatchOperationType, BatchUtils }
import scala.collection.JavaConverters._
import com.google.gdata.data.ILink._
import scala.concurrent.ExecutionContext.Implicits.global

trait BatchSender {

  val parent: Worksheet

  lazy val batchLink = parent.cellFeed.getLink(Rel.FEED_BATCH, Type.ATOM)

  private def sendBatchRequest(batchRequest: CellFeed): Future[Seq[Cell]] = {
    Future(parent.service.batch(new URL(batchLink.getHref), batchRequest)).map { batchResponseCellFeed =>
      val responseEntries = batchResponseCellFeed.getEntries.asScala
      val failures = responseEntries.filter(!BatchUtils.isSuccess(_)).map(BatchUtils.getBatchStatus(_))

      if (failures.isEmpty) {
        responseEntries.map(Cell(parent, _))
      } else {
        throw new IllegalStateException(s"Batch request failed: ${failures.head.getReason}")
      }
    }
  }

  private def buildBatchUpdateRequest(cellsWithTheirNewValues: Seq[(Cell, String)]): CellFeed = {
    val batchRequestCellFeed = new CellFeed()

    cellsWithTheirNewValues.foreach {
      case (cell, newValue) =>
        val batchEntry = new CellEntry(cell.googleEntry)
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
