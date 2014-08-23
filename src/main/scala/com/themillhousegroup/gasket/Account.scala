package com.themillhousegroup.gasket

import com.google.gdata.client.spreadsheet.SpreadsheetService
import java.net.URL
import com.google.gdata.data.spreadsheet.SpreadsheetFeed
import com.themillhousegroup.gasket.traits.Timing
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Account {
  def apply(username: String, password: String): Future[Account] = Future {
    val service = new SpreadsheetService("gasket")
    service.setUserCredentials(username, password)
    new Account(service)
  }
}

class Account(private[this] val service: SpreadsheetService) extends Timing {

  private[this] val SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")

  private[this] lazy val spreadsheetFeed = service.getFeed(SPREADSHEET_FEED_URL, classOf[SpreadsheetFeed])

  def spreadsheets: Future[Map[String, Spreadsheet]] = Future {
    import scala.collection.JavaConverters._
    time("spreadsheets fetch", spreadsheetFeed.getEntries).asScala.map(Spreadsheet(service, _)).map(s => s.title -> s).toMap
  }

}
