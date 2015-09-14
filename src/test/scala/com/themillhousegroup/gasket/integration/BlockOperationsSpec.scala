package com.themillhousegroup.gasket.integration

import org.specs2.mutable.Specification
import com.themillhousegroup.gasket.Account
import com.themillhousegroup.gasket.test.{ TestHelpers, GasketIntegrationSettings }
import com.themillhousegroup.gasket.test.ExampleSpreadsheetFetcher._

/**
 * For the purposes of these examples, there exists a spreadsheet
 * called "Example Spreadsheet" with worksheet, "BlockUpdate".
 * On this worksheet are 8 populated cells, with contents as follows:
 *
 *  Cell A1 	Cell B1
 *  Cell A2 	Cell B2
 *  Cell A3 	Cell B3
 *  Cell A4 	Cell B4
 *
 *  See GasketIntegrationSettings for information about how to set
 *  up a suitable file on your local system to hold credentials.
 */
class BlockOperationsSpec extends Specification with GasketIntegrationSettings with TestHelpers {

  val defaultCellValue = "Cell A1"
  val moddedValue = "MODIFIED"

  "Block Operations example" should {
    "fetch a block from a worksheet" in IntegrationScope { (username, p12File) =>

      val sheetAndCells = fetchSheetAndCells(username, p12File, "BlockUpdate")
      sheetAndCells._2 must not beEmpty

      sheetAndCells._2 must haveSize(8)
      sheetAndCells._2.head.value must beEqualTo(defaultCellValue)
      val sheet = sheetAndCells._1

      val twoByTwoBlock = waitFor(sheet.block(2 to 3, 1 to 2))

      twoByTwoBlock.rows.length must beEqualTo(2)
    }

    "compare two blocks logically" in IntegrationScope { (username, p12File) =>
      val sheetAndCells = fetchSheetAndCells(username, p12File, "BlockUpdate")
      sheetAndCells._2 must not beEmpty

      sheetAndCells._2 must haveSize(8)
      sheetAndCells._2.head.value must beEqualTo(defaultCellValue)
      val sheet = sheetAndCells._1

      val firstBlock = waitFor(sheet.block(2 to 3, 1 to 2))
      val secondBlock = waitFor(sheet.block(3 to 4, 1 to 2))

      firstBlock.compare(secondBlock) must be lessThan (0)
    }

    "make multiple changes" in IntegrationScope { (username, p12File) =>

      val sheetAndCells = fetchSheetAndCells(username, p12File, "BlockUpdate")
      sheetAndCells._2 must not beEmpty

      sheetAndCells._2 must haveSize(8)
      sheetAndCells._2.head.value must beEqualTo(defaultCellValue)
      val sheet = sheetAndCells._1

      val twoByTwoBlock = waitFor(sheet.block(2 to 3, 1 to 2))

      val dt = System.currentTimeMillis

      val initialValues = twoByTwoBlock.cells.map(_.value)

      val updatedValues = initialValues.map(_ + s" - Modified - $dt")

      val upResult = waitFor(twoByTwoBlock.update(updatedValues))

      upResult.cells must haveLength(4)

      val undoResult = waitFor(twoByTwoBlock.update(initialValues))

      undoResult.cells must haveLength(4)
    }
  }
}
