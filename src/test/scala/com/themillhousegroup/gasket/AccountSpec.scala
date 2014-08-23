package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.google.gdata.client.GoogleService.InvalidCredentialsException
import com.google.gdata.data.spreadsheet.{ SpreadsheetEntry, SpreadsheetFeed }
import java.net.URL
import com.themillhousegroup.gasket.test.{ TestFixtures, TestHelpers }
import com.google.gdata.client.spreadsheet.SpreadsheetService

class AccountSpec extends Specification with Mockito with TestHelpers with TestFixtures {

  class MockAccountScope(spreadsheetEntries: Seq[SpreadsheetEntry] = Nil) extends Scope {
    import scala.collection.JavaConverters._

    val mockService = mock[SpreadsheetService]
    mockService.setUserCredentials(org.mockito.Matchers.eq("bad"), anyString) throws new InvalidCredentialsException("denied")

    val mockSpreadsheetFeed = mock[SpreadsheetFeed]

    val jList = com.google.common.collect.Lists.newArrayList(spreadsheetEntries.asJava)
    mockSpreadsheetFeed.getEntries returns jList

    mockService.getFeed(any[URL], any[Class[SpreadsheetFeed]]) returns mockSpreadsheetFeed

    object TestAccount extends AccountBuilder {
      override val service = mockService
    }

    def gettingAccount(u: String = "good") = {
      waitFor(TestAccount(u, ""))
    }

    lazy val acct = gettingAccount()

    def gettingSpreadsheets = {
      waitFor(acct.spreadsheets)
    }
  }

  "Account access" should {
    "return a failed future for bad credentials" in new MockAccountScope {
      gettingAccount("bad") must throwAn[InvalidCredentialsException]
    }

    "return a future containing an Account when using good credentials" in new MockAccountScope {
      gettingAccount() must beAnInstanceOf[Account]
    }
  }

  "Account: Listing Spreadsheets" should {
    //    "Return an empty map if no spreadsheets exist" in new MockAccountScope() {
    //      gettingSpreadsheets must be empty
    //    }

    "Return a map where the keys are the spreadsheet-titles" in new MockAccountScope(Seq(s1, s2)) {

      gettingSpreadsheets.keys must containTheSameElementsAs(Seq("one", "two"))
    }
  }
}