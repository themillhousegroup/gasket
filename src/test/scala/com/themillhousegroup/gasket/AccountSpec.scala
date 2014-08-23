package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import scala.concurrent.Await
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.client.GoogleService.InvalidCredentialsException
import com.google.gdata.data.spreadsheet.{ SpreadsheetEntry, SpreadsheetFeed }
import java.net.URL
import com.google.gdata.data.PlainTextConstruct
import com.themillhousegroup.gasket.test.TestHelpers

class AccountSpec extends Specification with Mockito with TestHelpers {

  class MockAccountScope extends Scope {

    val mockService = mock[SpreadsheetService]
    val mockSpreadsheetFeed = mock[SpreadsheetFeed]
    mockService.getFeed(any[URL], any[Class[SpreadsheetFeed]]) returns mockSpreadsheetFeed

    object TestAccount extends AccountBuilder {
      override val service = mockService
    }

    def gettingAccount = {
      waitFor(TestAccount("", ""))
    }

    lazy val acct = gettingAccount

    def gettingSpreadsheets = {
      waitFor(acct.spreadsheets)
    }
  }

  "Account access" should {
    "return a failed future for bad credentials" in new MockAccountScope {
      mockService.setUserCredentials(anyString, anyString) throws new InvalidCredentialsException("nopes")
      gettingAccount must throwAn[InvalidCredentialsException]
    }

    "return a future containing an Account when using good credentials" in new MockAccountScope {
      gettingAccount must beAnInstanceOf[Account]
    }
  }

  "Account: Listing Spreadsheets" should {
    "Return an empty map if no spreadsheets exist" in new MockAccountScope {
      mockSpreadsheetFeed.getEntries returns new java.util.ArrayList[SpreadsheetEntry]()
      gettingSpreadsheets must be empty
    }

    "Return a map of where the keys are the spreadsheet-titles" in new MockAccountScope {
      val mockSpreadsheetOne = mock[SpreadsheetEntry]
      val mockSpreadsheetTwo = mock[SpreadsheetEntry]
      mockSpreadsheetOne.getTitle returns new PlainTextConstruct("one")
      mockSpreadsheetTwo.getTitle returns new PlainTextConstruct("two")

      mockSpreadsheetFeed.getEntries returns
        com.google.common.collect.Lists.newArrayList(mockSpreadsheetOne, mockSpreadsheetTwo)
      gettingSpreadsheets.keys must containTheSameElementsAs(Seq("one", "two"))
    }
  }
}
