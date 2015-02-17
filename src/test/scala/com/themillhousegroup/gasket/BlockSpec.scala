package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.test.{ TestFixtures, TestHelpers }
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.google.gdata.data.spreadsheet.{ CellFeed, CellEntry }
import com.google.gdata.client.spreadsheet.SpreadsheetService
import java.net.URL

class BlockSpec extends Specification with Mockito with TestHelpers with TestFixtures {

  val worksheet = mock[Worksheet]

  class BlockScope extends MockScope {
    val mockService = mock[SpreadsheetService]
  }

  "Rows function" should {

    "be able to split a rectangular seq of cells into its component rows" in new BlockScope {
      val twoRows = Seq(c1, c2, c3, c4).map(Cell(worksheet, _))
      val block = Block(worksheet, twoRows)
      val rows = block.rows
      rows must haveSize(2)
    }
  }
}
