package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.themillhousegroup.gasket.test.{ TestHelpers, TestFixtures }
import java.net.URL
import com.google.gdata.data.spreadsheet.{ WorksheetFeed, ListEntry, WorksheetEntry, CellFeed }
import scala.concurrent.Future
import scala.IllegalArgumentException
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.IFeed

class WorksheetSpec extends Specification with Mockito with TestHelpers with TestFixtures {

  trait WorksheetScope extends MockSpreadsheetScope {

    val w = Worksheet(mockService, mockSpreadsheet, mockWorksheetEntry)

    mockService.getFeed(any[URL], any[Class[CellFeed]]) returns mockCellFeed
    mockService.batch(any[URL], any[IFeed]) returns mockCellFeed
  }

  trait EmptyWorksheetScope extends MockSpreadsheetScope {

    val mockEmptyWorksheetEntry = mock[WorksheetEntry]
    mockEmptyWorksheetEntry.getRowCount returns 0

    val w = Worksheet(mockService, mockSpreadsheet, mockEmptyWorksheetEntry)
  }

  "Worksheet cells function" should {
    "return a sequence of converted CellEntry objects" in new WorksheetScope {
      waitFor(w.cells) must have size (4)
    }
  }

  "Worksheet rows function" should {
    "return a sequence of Row objects" in new WorksheetScope {
      val rowList = waitFor(w.rows)
      rowList must have size (2)

      rowList(0).cells must have size (2)
      rowList(1).cells must have size (2)
    }
  }

  "Worksheet headerLabels function" should {
    "provide the contents of the top row as a sequence of Strings" in new WorksheetScope {
      val headers = waitFor(w.headerLabels)
      headers must haveSize(2)
      headers must containTheSameElementsAs(Seq("R1C1", "R1C2"))
    }

    "return an empty sequence if there are no rows in the worksheet" in new EmptyWorksheetScope {
      val headers = waitFor(w.headerLabels)
      headers must haveSize(0)
    }
  }

  "Worksheet addRow function" should {

    "Reject rows with incorrect number of elements" in new WorksheetScope {
      mockSpreadsheet.worksheets returns Future.successful(Map(w.title -> w))

      waitFor(w.addRow(Seq("3"))) must throwAn[IllegalArgumentException]
      //
      //        .like {
      //        case iae: IllegalArgumentException => iae.getMessage must beEqualTo("Row: List(List(3)) were not of expected length 2")
      //      }

    }

    "call the underlying Google update function for each new row" in new WorksheetScope {
      mockSpreadsheet.worksheets returns Future.successful(Map(w.title -> w))

      val result = waitFor(w.addRow(Seq("1", "2")))
      result must beEqualTo(w)

      val result2 = waitFor(w.addRow(Seq("3", "4")))
      result2 must beEqualTo(w)

      there were two(mockService).insert(any[URL], any[ListEntry])
    }
  }

  "Worksheet addRows function" should {

    "call the Batch entry setup function for each new row" in new WorksheetScope {

      val threeNewRows = Seq(
        Seq("1", "2"),
        Seq("3", "4"),
        Seq("5", "6")
      )

      val result = waitFor(w.addRows(threeNewRows))
      result must beEqualTo(w)

      there were three(mockService).insert(any[URL], any[ListEntry])
    }

    "call the Batch operation only once" in new WorksheetScope {

      val threeNewRows = Seq(
        Seq("1", "2"),
        Seq("3", "4"),
        Seq("5", "6")
      )

      val result = waitFor(w.addRows(threeNewRows))
      result must beEqualTo(w)

      there was one(mockService).batch(any[URL], any[IFeed])
    }
  }

  "Worksheet clear function" should {

    "return a future version of the current sheet" in new WorksheetScope {
      mockWorksheetEntry.update returns mockWorksheetEntry

      val result = waitFor(w.clear)
      result must beEqualTo(w)
    }

    "call the underlying Google update function" in new WorksheetScope {
      mockWorksheetEntry.update returns mockWorksheetEntry

      val result = waitFor(w.clear)
      result must beEqualTo(w)

      there was one(mockWorksheetEntry).setRowCount(1)
    }
  }
}
