package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.themillhousegroup.gasket.test.{ TestHelpers, TestFixtures }
import java.net.URL
import com.google.gdata.data.spreadsheet.{ WorksheetEntry, CellFeed }
import org.specs2.specification.Scope
import com.google.gdata.client.spreadsheet.SpreadsheetService

class WorksheetSpec extends Specification with Mockito with TestFixtures with TestHelpers {
  val mockSpreadsheet = mock[Spreadsheet]

  mockService.getFeed(any[URL], any[Class[CellFeed]]) returns mockCellFeed

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
}
