package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.themillhousegroup.gasket.test.{ WorksheetFeedTestFixtures, TestFixtures, TestHelpers }
import java.net.URL
import com.google.gdata.data.spreadsheet.WorksheetFeed

class SpreadsheetSpec extends Specification with Mockito with TestHelpers with WorksheetFeedTestFixtures {

  mockService.getFeed(any[URL], any[Class[WorksheetFeed]]) returns mockWorksheetFeed

  "Spreadsheet" should {
    "convert worksheets to a map with worksheet title as key" in {
      val s = Spreadsheet(mockService, mockSpreadsheetEntry)
      val ws = waitFor(s.worksheets)
      ws.keys must containTheSameElementsAs(Seq("one", "two"))
    }
  }

}
