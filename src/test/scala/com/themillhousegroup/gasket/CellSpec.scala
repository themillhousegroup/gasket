package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.test.{ CellFeedTestFixtures, TestHelpers }
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.google.gdata.data.spreadsheet.CellEntry

class CellSpec extends Specification with Mockito with TestHelpers with CellFeedTestFixtures {

  val w = Worksheet(mockService, mockSpreadsheet, mockWorksheetEntry)
  val fCells = w.cells

  def mockCopyConstructor(ce: CellEntry): CellEntry = {
    val newCE = mockCellEntry(ce.getCell.getRow, ce.getCell.getCol)
    newCE.update returns newCE
    newCE
  }

  "Should return self if updating a cell to its current value" in {
    val cells = waitFor(fCells)
    val firstCell = cells.head
    val content = firstCell.value
    waitFor(firstCell.update(content)) must beEqualTo(firstCell)
  }

  "Should allow update by returning a Future version containing the new value" in {
    val fakeCell = Cell(w, c1, mockCopyConstructor)
    val content = fakeCell.value

    waitFor(fakeCell.update(content + "-modified")) must not be equalTo(fakeCell)
  }
}
