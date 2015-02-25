package com.themillhousegroup.gasket.integration

import org.specs2.mutable.Specification
import com.themillhousegroup.gasket.test.{ ExampleSpreadsheetFetcher, TestHelpers, GasketIntegrationSettings }
import ExampleSpreadsheetFetcher._
import scala.concurrent.{ Future, Await }
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

  "Clearing a worksheet" should {

    "Modify the worksheet both locally and remotely, leaving header row intact" in IntegrationScope { (username, password) =>

      val result = fetchSheetAndRows(username, password, "Sheet5")

      val numRows = result._2.size

      val sheet = result._1

      val rowToAdd = Seq(
        new Date().getTime.toString,
        InetAddress.getLocalHost.getHostName
      )

      val newLocalSheet = Await.result(sheet.addRows(Seq(rowToAdd)), shortWait)

      val newRows = Await.result(newLocalSheet.rows, shortWait)

      newRows.size must beEqualTo(numRows + 1)

      val clearedSheet = Await.result(newLocalSheet.clear, shortWait)

      Await.result(clearedSheet.rows, shortWait).size must beEqualTo(1)
    }

  }

}
