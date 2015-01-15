package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.test.{ CellFeedTestFixtures, TestHelpers }
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.google.gdata.data.spreadsheet.CellEntry

class RowSpec extends Specification with Mockito with TestHelpers with CellFeedTestFixtures {

  def mockCellAt(column: Int) = {
    val mockCell = mock[Cell]
    mockCell.colNumber returns column
    mockCell
  }

  val mockCell1 = mockCellAt(3)
  val mockCell2 = mockCellAt(6)
  val row = Row(1, Seq(mockCell1, mockCell2))

  "cellAt should return None if no cell at desired column" in {
    row.cellAt(0) must beNone
    row.cellAt(1) must beNone
  }

  "cellAt should return the cell at desired column" in {
    row.cellAt(3) must beSome(mockCell1)
    row.cellAt(6) must beSome(mockCell2)
  }
}
