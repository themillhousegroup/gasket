package com.themillhousegroup.gasket

import com.google.gdata.client.spreadsheet.SpreadsheetService
import scala.util.Try
import java.net.URL
import com.google.gdata.data.spreadsheet.{ SpreadsheetEntry, SpreadsheetFeed }

object Account {
  def apply(username: String, password: String): Try[Account] = Try {
    val service = new SpreadsheetService("gasket")
    service.setUserCredentials(username, password)
    new Account(service)
  }
}

class Account(private[this] val service: SpreadsheetService) {

  private[this] val SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")

  private[this] lazy val spreadsheetFeed = service.getFeed(SPREADSHEET_FEED_URL, classOf[SpreadsheetFeed])

  def spreadsheets: Map[String, Spreadsheet] = {
    import scala.collection.JavaConverters._
    spreadsheetFeed.getEntries.asScala.map(Spreadsheet(service, _)).map(s => s.title -> s).toMap
  }

}
