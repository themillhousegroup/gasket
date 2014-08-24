package com.themillhousegroup.gasket.integration

import org.specs2.mutable.Specification
import com.themillhousegroup.gasket.Account
import scala.concurrent.Await
import com.themillhousegroup.gasket.test.GasketIntegrationSettings

/**
 * For the purposes of these examples, there exists a spreadsheet
 * called "Example Spreadsheet" with one worksheet, "Sheet1".
 * On that worksheet are 9 populated cells, with contents as follows:
 *
 *  Top Left	    Top Middle	    Top Right
 *  Center Left	  Center Middle	  Center Right
 *  Bottom Left	  Bottom Middle	  Bottom Right
 *
 *
 */
class ForComprehensionSpec extends Specification with GasketIntegrationSettings {

  "For Comprehension example" should {
    "get all nine cells" in {

      val futureCells =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          cells <- ws("Sheet1").cells
        } yield cells

      val result = Await.result(futureCells, timeout)
      result must not beEmpty

      result must haveSize(9)
    }

    "get all nine cells as a Seq[String]" in {

      val futureCellContents =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          cells <- ws("Sheet1").cells
          contents = cells.map(_.value)
        } yield contents

      val result = Await.result(futureCellContents, timeout)
      result must not beEmpty

      result must haveSize(9)
      result.head must beEqualTo("Top Left")
      result.last must beEqualTo("Bottom Right")
    }

    "get all three rows" in {

      val futureRows =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          rows <- ws("Sheet1").rows
        } yield rows

      val result = Await.result(futureRows, timeout)
      result must not beEmpty

      result must haveSize(3)
    }

    "get a rectangular block of just the top-right 4 cells" in {

      val futureRows =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          rows <- ws("Sheet1").block(1 to 2, 2 to 3)
        } yield rows

      val blockRows = Await.result(futureRows, timeout)
      blockRows must not beEmpty

      blockRows must haveSize(2)

      blockRows.head.cells.head.value must beEqualTo("Top Middle")
      blockRows.last.cells.last.value must beEqualTo("Center Right")
    }

  }
}
