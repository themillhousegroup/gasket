package com.themillhousegroup.gasket.test

import com.themillhousegroup.gasket._
import scala.concurrent.{ Future, Await }
import com.themillhousegroup.gasket.Worksheet
import com.themillhousegroup.gasket.Row
import java.io.File
import scala.collection.mutable.Map

object ExampleSpreadsheetFetcher extends TestHelpers {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val credentialAccountMap: scala.collection.mutable.Map[String, Future[Account]] = scala.collection.mutable.Map()

  private def lazyFetchAccount(clientId: String, p12File: File): Future[Account] = {
    if (!credentialAccountMap.contains(clientId)) {
      credentialAccountMap.put(clientId, Account(clientId, p12File))
    }
    credentialAccountMap(clientId)
  }

  def fetchSheet(clientId: String, p12File: File, sheetName: String): Future[Worksheet] = {
    for {
      acct <- lazyFetchAccount(clientId, p12File)
      ss <- acct.spreadsheets
      ws <- ss("Example Spreadsheet").worksheets
      sheet = ws(sheetName)
    } yield (sheet)
  }

  def withSheet[T](clientId: String, p12File: File, sheetName: String)(f: (Worksheet) => Future[T]): (Worksheet, T) = {
    Await.result({
      for {
        sheet <- fetchSheet(clientId, p12File, sheetName)
        r <- f(sheet)
      } yield (sheet, r)
    }, shortWait)
  }

  def fetchSheetAndRows(clientId: String, p12File: File, sheetName: String): (Worksheet, Seq[Row]) = {
    withSheet(clientId, p12File, sheetName)(_.rows)
  }

  def fetchSheetAndCells(clientId: String, p12File: File, sheetName: String): (Worksheet, Seq[Cell]) = {
    withSheet(clientId, p12File, sheetName)(_.cells)
  }
}
