package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.test.{ TestFixtures, TestHelpers }
import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.google.gdata.data.spreadsheet.{ CellFeed, CellEntry }
import com.google.gdata.client.spreadsheet.SpreadsheetService
import java.net.URL

class BlockSpec extends Specification with Mockito with TestHelpers with TestFixtures {

  "Rows function" should {

    "be able to split a rectangular seq of cells into its component rows" in {

    }
  }
}
