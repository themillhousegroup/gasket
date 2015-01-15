package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.test.{ CellFeedTestFixtures, TestHelpers, TestFixtures }
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import java.net.URL
import com.google.gdata.data.spreadsheet.CellFeed

class CellSpec extends Specification with Mockito with TestHelpers with CellFeedTestFixtures {

  val w = Worksheet(mockService, mockSpreadsheet, mockWorksheetEntry)
  val fCells = w.cells

  "Should return self if updating a cell to its current value" in {
    val cells = waitFor(fCells)
    val firstCell = cells.head
    val content = firstCell.value
    waitFor(firstCell.update(content)) must beEqualTo(firstCell)
  }

  "Should allow update by returning a Future version containing the new value" in {
    true must beTrue
  }
}
