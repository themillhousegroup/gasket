package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.themillhousegroup.gasket.test.{ CellFeedTestFixtures, TestHelpers, TestFixtures }
import java.net.URL
import com.google.gdata.data.spreadsheet.{ ListEntry, WorksheetEntry, CellFeed }
import org.specs2.specification.Scope
import com.google.gdata.client.spreadsheet.SpreadsheetService
import scala.concurrent.Future

class WorksheetSpec extends Specification with Mockito with TestHelpers with CellFeedTestFixtures {

  class WorksheetScope extends Scope {
    val w = Worksheet(mockService, mockSpreadsheet, mockWorksheetEntry)
  }

  class EmptyWorksheetScope extends Scope {
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

  "Worksheet addRows function" should {

    "return self if no rows are to be added" in new WorksheetScope {
      mockSpreadsheet.worksheets returns Future.successful(Map(w.title -> w))

      val result = waitFor(w.addRows(Nil))
      result must beEqualTo(w)
    }

    "call the underlying Google update function for each new row" in new WorksheetScope {
      mockSpreadsheet.worksheets returns Future.successful(Map(w.title -> w))

      val threeNewRows = Seq(
        Seq("foo" -> "1", "bar" -> "2"),
        Seq("foo" -> "3", "bar" -> "4"),
        Seq("foo" -> "5", "bar" -> "6")
      )

      val result = waitFor(w.addRows(threeNewRows))
      result must beEqualTo(w)

      there were three(mockService).insert(any[URL], any[ListEntry])
    }
  }
}
