package com.themillhousegroup.gasket.integration

import org.specs2.mutable.Specification
import com.themillhousegroup.gasket.Account
import scala.concurrent.Await
import com.themillhousegroup.gasket.test.{ TestHelpers, GasketIntegrationSettings }
import com.themillhousegroup.gasket.test.ExampleSpreadsheetFetcher._

/**
 * For the purposes of these examples, there exists a spreadsheet
 * called "Example Spreadsheet" with worksheets, "Sheet2" and "Sheet3".
 * On each worksheet is 1 populated cell, with contents as follows:
 *
 *  Cell A1
 *
 *  See GasketIntegrationSettings for information about how to set
 *  up a suitable file on your local system to hold credentials.
 */
class CellUpdateSpec extends Specification with GasketIntegrationSettings with TestHelpers {

  val defaultCellValue = "Cell A1"
  val moddedValue = "MODIFIED"

  "Cell Update example" should {
    "make a single change" in IntegrationScope { (username, password) =>

      val result = fetchSheetAndCells(username, password, "Sheet2")._2
      result must not beEmpty

      result must haveSize(1)
      result.head.value must beEqualTo(defaultCellValue)

      val futureModResult = result.head.update(moddedValue)

      val modResult = Await.result(futureModResult, shortWait)

      modResult.value must beEqualTo(moddedValue)

      val futureRollbackResult = modResult.update(defaultCellValue)

      val rollbackResult = Await.result(futureRollbackResult, shortWait)

      rollbackResult.value must beEqualTo(defaultCellValue)

    }

    "make a single change as part of a for-comprehension" in IntegrationScope { (username, password) =>

      val futureCell =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          cells <- ws("Sheet3").cells
          newCell <- cells.head.update(moddedValue)
        } yield newCell

      Await.result(futureCell, shortWait).value must beEqualTo(moddedValue)

      val rolledBackCell =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          cells <- ws("Sheet3").cells
          newCell <- cells.head.update(defaultCellValue)
        } yield newCell

      Await.result(rolledBackCell, shortWait).value must beEqualTo(defaultCellValue)
    }

  }
}
