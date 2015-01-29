package com.themillhousegroup.gasket.integration

import org.specs2.mutable.Specification
import com.themillhousegroup.gasket.test.{ TestHelpers, GasketIntegrationSettings }
import scala.concurrent.Await
import com.themillhousegroup.gasket.{ Worksheet, Row, Account }
import java.util.Date
import java.net.{ InetAddress, Inet4Address }

/**
 * For the purposes of these examples, there exists a spreadsheet
 * called "Example Spreadsheet" with worksheet, "Sheet4", that has
 * column headers:
 * Timestamp | Hostname | Optional
 * This worksheet will get a row added to it each time this integration
 * test is run, with format of either:
 * <timestamp>, <hostname>, [blank]
 * or
 * <timestamp>, <hostname>, "Full Row"
 *
 *
 *  See GasketIntegrationSettings for information about how to set
 *  up a suitable file on your local system to hold credentials.
 */
class WorksheetAddRowsSpec extends Specification with GasketIntegrationSettings with TestHelpers {

  isolated
  sequential

  def fetchSheetAndRows(username: String, password: String): (Worksheet, Seq[Row]) = {
    val futureRows =
      for {
        acct <- Account(username, password)
        ss <- acct.spreadsheets
        ws <- ss("Example Spreadsheet").worksheets
        sheet4 = ws("Sheet4")
        rows <- sheet4.rows
      } yield (sheet4, rows)

    Await.result(futureRows, shortWait)
  }

  "Adding (partial) rows to worksheet" should {

    "Modify the worksheet both locally and remotely" in IntegrationScope { (username, password) =>

      val result = fetchSheetAndRows(username, password)

      val numRows = result._2.size
      numRows must beGreaterThanOrEqualTo(1)

      val sheet = result._1

      val rowToAdd = Seq(
        ("Timestamp" -> new Date().getTime.toString),
        ("Hostname" -> InetAddress.getLocalHost.getHostName)
      )

      val newLocalSheet = Await.result(sheet.addRows(Seq(rowToAdd)), shortWait)

      val newRows = Await.result(newLocalSheet.rows, shortWait)

      newRows.size must beEqualTo(numRows + 1)
    }
  }

  "Adding full rows to worksheet" should {

    "Modify the worksheet both locally and remotely" in IntegrationScope { (username, password) =>

      val result = fetchSheetAndRows(username, password)

      val numRows = result._2.size
      numRows must beGreaterThanOrEqualTo(1)

      val sheet = result._1

      val rowToAdd = Seq(
        new Date().getTime.toString,
        InetAddress.getLocalHost.getHostName,
        "Full Row"
      )

      val newLocalSheet = Await.result(sheet.addFullRows(Seq(rowToAdd)), shortWait)

      val newRows = Await.result(newLocalSheet.rows, shortWait)

      newRows.size must beEqualTo(numRows + 1)
    }
  }
}
