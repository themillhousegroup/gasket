package com.themillhousegroup.gasket

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import com.google.gdata.client.GoogleService.InvalidCredentialsException
import com.google.gdata.data.spreadsheet.{ SpreadsheetEntry, SpreadsheetFeed }
import java.net.URL
import java.io.File
import com.themillhousegroup.gasket.test.{ TestFixtures, TestHelpers }
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.IFeed
import scala.collection.JavaConverters._
import scala.concurrent.Future
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential

class AccountSpec extends Specification with Mockito with TestHelpers with TestFixtures {
  val mockFile = mock[java.io.File]

  class MockAccountScope(spreadsheetEntries: Seq[SpreadsheetEntry] = Nil) extends MockScope {

    val mockService = mock[SpreadsheetService]
    val mockBuilder = mock[(String, File) => Future[GoogleCredential]]
    val mockCredential = mock[GoogleCredential]
    mockBuilder.apply(org.mockito.Matchers.eq("good"), any[File]) returns Future.successful(mockCredential)
    mockBuilder.apply(org.mockito.Matchers.eq("bad"), any[File]) returns Future.failed(new InvalidCredentialsException("denied"))

    object TestAccount extends AccountBuilder {
      override lazy val service = mockService
      override def buildCredential(clientId: String, p12: File) = mockBuilder(clientId, p12)
    }

    def gettingAccount(cId: String = "good") = {
      waitFor(TestAccount(cId, mockFile))
    }

    lazy val acct = gettingAccount()

    def givenSpreadsheetEntries(spreadsheetEntries: Seq[SpreadsheetEntry] = Nil) = {
      val jList = com.google.common.collect.Lists.newArrayList(spreadsheetEntries.asJava)
      mockSpreadsheetFeed.getEntries returns jList
    }

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

    "Return a map where the keys are the spreadsheet-titles" in new MockAccountScope {
      givenSpreadsheetEntries(Seq(s1, s2))
      gettingSpreadsheets.keys must containTheSameElementsAs(Seq("one", "two"))
    }
  }
}
