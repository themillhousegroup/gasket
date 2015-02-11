package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.test.{ TestFixtures, TestHelpers }
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.google.gdata.data.spreadsheet.{ CellFeed, CellEntry }
import com.google.gdata.client.spreadsheet.SpreadsheetService
import java.net.URL

class CellSpec extends Specification with Mockito with TestHelpers with TestFixtures {

  class CellScope extends MockScope {
    val mockService = mock[SpreadsheetService]
    val w = Worksheet(mockService, mockSpreadsheet, mockWorksheetEntry)
    mockService.getFeed(any[URL], any[Class[CellFeed]]) returns mockCellFeed
  }

  def mockCopyConstructor(ce: CellEntry): CellEntry = {
    val newCE = mockCellEntry(ce.getCell.getRow, ce.getCell.getCol)
    newCE.update returns newCE
    newCE
  }

  "Cell update" should {

    "Return self if updating a cell to its current value" in new CellScope {
      val fCells = w.cells

      val cells = waitFor(fCells)
      val firstCell = cells.head
      val content = firstCell.value
      waitFor(firstCell.update(content)) must beEqualTo(firstCell)
    }

    "Allow update by returning a Future version containing the new value" in new CellScope {

      val fakeCell = Cell(w, c1, mockCopyConstructor)
      val content = fakeCell.value

      waitFor(fakeCell.update(content + "-modified")) must not be equalTo(fakeCell)
    }
  }

  "Field access" should {

    "Consider an empty string as a None in valueOption" in new CellScope {
      val cellEntry = mockCellEntry(1, 2, Some(""))

      val fakeCell = Cell(w, cellEntry, mockCopyConstructor)

      fakeCell.valueOption must beNone
    }

    "Consider a whitespace-only string as a None in valueOption" in new CellScope {
      val cellEntry = mockCellEntry(1, 2, Some("   "))

      val fakeCell = Cell(w, cellEntry, mockCopyConstructor)

      fakeCell.valueOption must beNone
    }

    "Consider a populated string as a None in valueOption" in new CellScope {
      val cellEntry = mockCellEntry(1, 2, Some("foo"))

      val fakeCell = Cell(w, cellEntry, mockCopyConstructor)

      fakeCell.valueOption must beSome("foo")
    }

    "Consider an empty string as a None in numericValueOption" in new CellScope {
      val cellEntry = mockCellEntry(1, 2, Some(""))

      val fakeCell = Cell(w, cellEntry, mockCopyConstructor)

      fakeCell.numericValueOption must beNone
    }

  }
}
