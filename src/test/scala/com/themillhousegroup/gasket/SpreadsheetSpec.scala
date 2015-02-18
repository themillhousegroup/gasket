package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.themillhousegroup.gasket.test.{ TestFixtures, TestHelpers }
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet.{ WorksheetFeed, SpreadsheetFeed }
import java.net.URL

class SpreadsheetSpec extends Specification with Mockito with TestHelpers with TestFixtures {

  trait SpreadsheetScope extends MockSpreadsheetScope {
    mockService.getFeed(any[URL], any[Class[WorksheetFeed]]) returns mockWorksheetFeed
  }

  "Spreadsheet" should {
    "convert worksheets to a map with worksheet title as key" in new SpreadsheetScope {
      val s = Spreadsheet(mockService, mockSpreadsheetEntry)
      val ws = waitFor(s.worksheets)
      ws.keys must containTheSameElementsAs(Seq("one", "two"))
    }
  }

}
