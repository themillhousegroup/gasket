package com.themillhousegroup.gasket.integration

import org.specs2.mutable.Specification
import com.themillhousegroup.gasket.Account
import scala.concurrent.Await
import com.themillhousegroup.gasket.test.{ TestHelpers, GasketIntegrationSettings }

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
 *  See GasketIntegrationSettings for information about how to set
 *  up a suitable file on your local system to hold credentials.
 */
class ForComprehensionSpec extends Specification with GasketIntegrationSettings with TestHelpers {

  "For Comprehension example" should {
    "get all nine cells" in IntegrationScope { (username, password) =>

      val futureCells =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          cells <- ws("Sheet1").cells
        } yield cells

      val result = Await.result(futureCells, shortWait)
      result must not beEmpty

      result must haveSize(9)
    }

    "get all nine cells as a Seq[String]" in IntegrationScope { (username, password) =>

      val futureCellContents =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          cells <- ws("Sheet1").cells
          contents = cells.map(_.value)
        } yield contents

      val result = Await.result(futureCellContents, shortWait)
      result must not beEmpty

      result must haveSize(9)
      result.head must beEqualTo("Top Left")
      result.last must beEqualTo("Bottom Right")
    }

    "get all three rows" in IntegrationScope { (username, password) =>

      val futureRows =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          rows <- ws("Sheet1").rows
        } yield rows

      val result = Await.result(futureRows, shortWait)
      result must not beEmpty

      result must haveSize(3)
    }

    "get a rectangular block of just the top-right 4 cells" in IntegrationScope { (username, password) =>

      val futureRows =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          rows <- ws("Sheet1").blockRows(1 to 2, 2 to 3)
        } yield rows

      val blockRows = Await.result(futureRows, shortWait)
      blockRows must not beEmpty

      blockRows must haveSize(2)

      blockRows.head.cells.head.value must beEqualTo("Top Middle")
      blockRows.last.cells.last.value must beEqualTo("Center Right")
    }

    "be able to iterate over the cells with their header labels in tuples" in IntegrationScope { (username, password) =>

      val futureTuples =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          w = ws("Sheet1")
          cells <- w.cells
          tuples <- w.withHeaderLabels(cells)
        } yield tuples

      val headerCellTuples = Await.result(futureTuples, shortWait)

      headerCellTuples must haveSize(9)

      val headersAndContents = headerCellTuples.map(t => t._1 -> t._2.value)

      headersAndContents(3) must beEqualTo("Top Left" -> "Center Left")
      headersAndContents(5) must beEqualTo("Top Right" -> "Center Right")

      headersAndContents(6) must beEqualTo("Top Left" -> "Bottom Left")
      headersAndContents(8) must beEqualTo("Top Right" -> "Bottom Right")
    }

  }
}
