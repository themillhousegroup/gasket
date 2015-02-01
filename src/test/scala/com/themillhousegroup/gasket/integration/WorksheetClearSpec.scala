package com.themillhousegroup.gasket.integration

import org.specs2.mutable.Specification
import com.themillhousegroup.gasket.test.{ TestHelpers, GasketIntegrationSettings }
import scala.concurrent.Await
import com.themillhousegroup.gasket.{ Worksheet, Row, Account }
import java.util.Date
import java.net.InetAddress

/**
 * For the purposes of these examples, there exists a spreadsheet
 * called "Example Spreadsheet" with worksheet, "Sheet5", that has
 * column headers:
 * Timestamp | Hostname
 * This worksheet will get some random number of rows added to it each time this integration
 * test is run, with format of:
 * <timestamp>, <hostname>
 *
 *  See GasketIntegrationSettings for information about how to set
 *  up a suitable file on your local system to hold credentials.
 */
class WorksheetClearSpec extends Specification with GasketIntegrationSettings with TestHelpers {

  isolated
  sequential

  def fetchSheetAndRows(username: String, password: String): (Worksheet, Seq[Row]) = {
    val futureRows =
      for {
        acct <- Account(username, password)
        ss <- acct.spreadsheets
        ws <- ss("Example Spreadsheet").worksheets
        sheet5 = ws("Sheet5")
        rows <- sheet5.rows
      } yield (sheet5, rows)

    Await.result(futureRows, shortWait)
  }

  // FIXME: Getting InvalidEntryException: : Bad Request when calling this.

  //  "Clearing a worksheet" should {
  //
  //    "Modify the worksheet both locally and remotely" in IntegrationScope { (username, password) =>
  //
  //      val result = fetchSheetAndRows(username, password)
  //
  //      val numRows = result._2.size
  //      numRows must beGreaterThanOrEqualTo(1)
  //
  //      val sheet = result._1
  //
  //      val rowToAdd = Seq(
  //        ("Timestamp" -> new Date().getTime.toString),
  //        ("Hostname" -> InetAddress.getLocalHost.getHostName)
  //      )
  //
  //      val newLocalSheet = Await.result(sheet.addRows(Seq(rowToAdd)), shortWait)
  //
  //      val newRows = Await.result(newLocalSheet.rows, shortWait)
  //
  //      newRows.size must beEqualTo(numRows + 1)
  //
  //      val clearedSheet = Await.result(newLocalSheet.clear, shortWait)
  //
  //      Await.result(clearedSheet.rows, shortWait).size must beEqualTo(0)
  //    }
  //  }

}
