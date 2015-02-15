package com.themillhousegroup.gasket.test

import com.themillhousegroup.gasket._
import scala.concurrent.{ Future, Await }
import com.themillhousegroup.gasket.Worksheet
import com.themillhousegroup.gasket.Row

object ExampleSpreadsheetFetcher extends TestHelpers {
  import scala.concurrent.ExecutionContext.Implicits.global

  def fetchSheet(username: String, password: String, sheetName: String): Future[Worksheet] = {
    for {
      acct <- Account(username, password)
      ss <- acct.spreadsheets
      ws <- ss("Example Spreadsheet").worksheets
      sheet = ws(sheetName)
    } yield (sheet)
  }

  def withSheet[T](username: String, password: String, sheetName: String)(f: (Worksheet) => Future[T]): (Worksheet, T) = {
    Await.result({
      for {
        sheet <- fetchSheet(username, password, sheetName)
        r <- f(sheet)
      } yield (sheet, r)
    }, shortWait)
  }

  def fetchSheetAndRows(username: String, password: String, sheetName: String): (Worksheet, Seq[Row]) = {
    withSheet(username, password, sheetName)(_.rows)
  }

  def fetchSheetAndCells(username: String, password: String, sheetName: String): (Worksheet, Seq[Cell]) = {
    withSheet(username, password, sheetName)(_.cells)
  }

}
