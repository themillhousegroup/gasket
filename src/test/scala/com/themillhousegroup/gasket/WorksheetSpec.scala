package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.themillhousegroup.gasket.test.{ TestHelpers, TestFixtures }
import java.net.URL
import com.google.gdata.data.spreadsheet.CellFeed

class WorksheetSpec extends Specification with Mockito with TestFixtures with TestHelpers {
  val mockSpreadsheet = mock[Spreadsheet]

  mockService.getFeed(any[URL], any[Class[CellFeed]]) returns mockCellFeed

  "Worksheet cells function" should {
    "return a sequence of converted CellEntry objects" in {
      val w = Worksheet(mockService, mockSpreadsheet, mockWorksheetEntry)
      waitFor(w.cells) must have size (4)
    }
  }

  "Worksheet rows function" should {
    "return a sequence of Row objects" in {
      val w = Worksheet(mockService, mockSpreadsheet, mockWorksheetEntry)
      val rowList = waitFor(w.rows)
      rowList must have size (2)

      rowList(0).cells must have size (2)
      rowList(1).cells must have size (2)
    }
  }
}
