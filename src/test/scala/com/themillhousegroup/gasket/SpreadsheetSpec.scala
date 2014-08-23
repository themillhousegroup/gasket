package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.google.gdata.client.spreadsheet.SpreadsheetService
import java.net.URL
import com.google.gdata.data.spreadsheet.{ SpreadsheetEntry, WorksheetEntry, WorksheetFeed }
import com.google.gdata.data.PlainTextConstruct
import com.themillhousegroup.gasket.test.TestHelpers

class SpreadsheetSpec extends Specification with Mockito with TestHelpers {

  val mockService = mock[SpreadsheetService]
  val mockSpreadsheetEntry = mock[SpreadsheetEntry]
  mockSpreadsheetEntry.getWorksheetFeedUrl returns new URL("http://localhost")

  val mockWorksheetFeed = mock[WorksheetFeed]

  mockService.getFeed(any[URL], any[Class[WorksheetFeed]]) returns mockWorksheetFeed

  val w1 = mock[WorksheetEntry]
  val w2 = mock[WorksheetEntry]
  w1.getTitle returns new PlainTextConstruct("one")
  w2.getTitle returns new PlainTextConstruct("two")

  mockWorksheetFeed.getEntries returns com.google.common.collect.Lists.newArrayList(w1, w2)

  "Spreadsheet" should {
    "convert worksheets to a map with worksheet title as key" in {
      val s = Spreadsheet(mockService, mockSpreadsheetEntry)
      val ws = waitFor(s.worksheets)
      ws.keys must containTheSameElementsAs(Seq("one", "two"))
    }
  }

}
