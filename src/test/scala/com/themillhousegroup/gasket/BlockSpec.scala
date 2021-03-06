package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.test.{ TestFixtures, TestHelpers }
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.google.gdata.data.spreadsheet.{ CellFeed, CellEntry }
import com.google.gdata.client.spreadsheet.SpreadsheetService
import java.net.URL
import com.themillhousegroup.gasket.helpers.BatchSender
import scala.concurrent.Future

class BlockSpec extends Specification with Mockito with TestHelpers with TestFixtures {

  val worksheet = mock[Worksheet]

  "Rows function" should {

    "be able to split a single-rowed sequence into a single row" in new MockSpreadsheetScope {
      val oneRow = Seq(c1, c2).map(Cell(worksheet, _))
      val block = Block(worksheet, oneRow)
      val rows = block.rows

      block.minRow must beEqualTo(1)
      rows must haveSize(1)
    }

    "be able to split a rectangular seq of cells into its component rows" in new MockSpreadsheetScope {
      val twoRows = Seq(c1, c2, c3, c4).map(Cell(worksheet, _))
      val block = Block(worksheet, twoRows)
      val rows = block.rows

      block.height must beEqualTo(2)
      rows must haveSize(2)
    }
  }

  "Update function" should {

    "Reject a sequence of new values that is incorrectly sized" in new MockSpreadsheetScope {
      val twoRows = Seq(c1, c2, c3, c4).map(Cell(worksheet, _))
      val block = Block(worksheet, twoRows)

      waitFor(block.update(Seq())) must throwAn[IllegalArgumentException]
    }

    "Return a new Block containing the updated values" in new MockSpreadsheetScope {
      val twoRows = Seq(c1, c2, c3, c4).map(Cell(worksheet, _))
      val block = new Block(worksheet, twoRows) {
        override lazy val batchSender = mock[BatchSender]
        batchSender.sendBatchUpdate(any[Seq[Cell]], any[Seq[String]]) returns Future.successful(Seq(c4, c3, c2, c1).map(Cell(worksheet, _)))
      }

      val result = waitFor(block.update(Seq("a", "b", "c", "d")))
      result must not be equalTo(block)
    }
  }
}
