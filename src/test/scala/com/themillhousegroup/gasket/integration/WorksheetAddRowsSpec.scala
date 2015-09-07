package com.themillhousegroup.gasket.integration

import org.specs2.mutable.Specification
import com.themillhousegroup.gasket.test.{ ExampleSpreadsheetFetcher, TestHelpers, GasketIntegrationSettings }
import ExampleSpreadsheetFetcher._
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

  //  "Adding a single row to worksheet" should {
  //
  //    "Modify the worksheet both locally and remotely" in IntegrationScope { (username, password) =>
  //
  //      val result = fetchSheetAndRows(username, password, "Sheet4")
  //
  //      val numRows = result._2.size
  //      numRows must beGreaterThanOrEqualTo(1)
  //
  //      val sheet = result._1
  //
  //      val rowToAdd = Seq(new Date().getTime.toString, InetAddress.getLocalHost.getHostName, "Single Row Add")
  //
  //      val newLocalSheet = Await.result(sheet.addRow(rowToAdd), shortWait)
  //
  //      val newRows = Await.result(newLocalSheet.rows, shortWait)
  //
  //      newRows.size must beEqualTo(numRows + 1)
  //    }
  //  }

  "Adding multiple rows to worksheet" should {

    "Modify the worksheet both locally and remotely - single row case" in IntegrationScope { (clientId, p12File) =>

      val result = fetchSheetAndRows(clientId, p12File, "Sheet4")

      val numRows = result._2.size
      numRows must beGreaterThanOrEqualTo(1)

      val sheet = result._1

      val rowToAdd = Seq(
        new Date().getTime.toString,
        InetAddress.getLocalHost.getHostName,
        "Multi Row - but just one"
      )

      val newLocalSheet = Await.result(sheet.addRows(Seq(rowToAdd)), shortWait)

      val newRows = Await.result(newLocalSheet.rows, shortWait)

      newRows.size must beEqualTo(numRows + 1)
    }
  }
}
