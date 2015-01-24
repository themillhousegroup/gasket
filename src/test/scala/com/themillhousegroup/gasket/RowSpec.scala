package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.test.{ CellFeedTestFixtures, TestHelpers }
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.google.gdata.data.spreadsheet.CellEntry
import com.google.gdata.data.TextConstruct

class RowSpec extends Specification with Mockito with TestHelpers with CellFeedTestFixtures {

  def mockCellAt(column: Int, columnName: String) = {
    val mockTitle = mock[TextConstruct]
    mockTitle.getPlainText returns s"${columnName}99"

    val mockEntry = mock[CellEntry]
    mockEntry.getTitle returns mockTitle

    val mockCell = mock[Cell]
    mockCell.googleEntry returns mockEntry
    mockCell.colNumber returns column
    mockCell.value returns s"$columnName $column"
    mockCell
  }

  val mockCell1 = mockCellAt(3, "C")
  val mockCell2 = mockCellAt(6, "F")
  val row = Row(1, Seq(mockCell1, mockCell2))

  "cellAt(Int)" should {

    "return None if no cell at desired column" in {
      row.cellAt(0) must beNone
      row.cellAt(1) must beNone
    }

    "return the cell at desired column" in {
      row.cellAt(3) must beSome(mockCell1)
      row.cellAt(6) must beSome(mockCell2)
    }
  }

  "cellAt(String)" should {

    "return None if no cell at desired column" in {
      row.cellAt("A") must beNone
      row.cellAt("B") must beNone
    }

    "return the cell at desired column" in {
      row.cellAt("C") must beSome(mockCell1)
      row.cellAt("F") must beSome(mockCell2)
    }
  }

  "values" should {
    "return a sequence of strings" in {
      row.values must beEqualTo(Seq("C 3", "F 6"))
    }
  }

  "columnValues" should {
    "return a sequence of (Int, String) representing the column -> value" in {
      row.columnValues must beEqualTo(Seq(3 -> "C 3", 6 -> "F 6"))
    }
  }
}
