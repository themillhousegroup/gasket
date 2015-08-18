package com.themillhousegroup.gasket

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.gdata.client.spreadsheet.SpreadsheetService
import java.net.URL
import java.io.File
import com.google.gdata.data.spreadsheet.SpreadsheetFeed
import com.themillhousegroup.gasket.traits.Timing
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

object Account extends AccountBuilder {
  private[gasket] val SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")

  // http://stackoverflow.com/questions/30483601/create-spreadsheet-using-google-spreadsheet-api-in-google-drive-in-java
  //private[gasket] val CLIENT_ID = "1078815581589-ckv4dcn0f9lruaf4o678lu5edjk5kdma@developer.gserviceaccount.com" // notasecret

  val scopesArray = List(
    "https://spreadsheets.google.com/feeds",
    "https://spreadsheets.google.com/feeds/spreadsheets/private/full",
    "https://docs.google.com/feeds").asJava

  override lazy val service = new SpreadsheetService("gasket")

}

// Solely to allow a mocked SpreadsheetService for testing
protected[this] class AccountBuilder {
  lazy val service = new SpreadsheetService("gasket")

  def apply(clientId: String, p12: File): Future[Account] = Future {
    val credential = new GoogleCredential.Builder().
      setServiceAccountScopes(Account.scopesArray).
      setServiceAccountId(clientId).
      setServiceAccountPrivateKeyFromP12File(p12).
      build()

    service.setOAuth2Credentials(credential)
    new Account(service)
  }
}

class Account(private[this] val service: SpreadsheetService) extends Timing {
  import Account._

  private[this] lazy val spreadsheetFeed = service.getFeed(SPREADSHEET_FEED_URL, classOf[SpreadsheetFeed])

  def spreadsheets: Future[Map[String, Spreadsheet]] = Future {
    import scala.collection.JavaConverters._
    time("spreadsheets fetch", spreadsheetFeed.getEntries).asScala.map(Spreadsheet(service, _)).map(s => s.title -> s).toMap
  }

}
