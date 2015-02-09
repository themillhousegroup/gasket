package com.themillhousegroup.gasket.test

import com.themillhousegroup.gasket.{ Cell, Account, Row, Worksheet }
import scala.concurrent.Await

object ExampleSpreadsheetFetcher extends TestHelpers {
  import scala.concurrent.ExecutionContext.Implicits.global

  //TODO: DRY these two up

  def fetchSheetAndRows(username: String, password: String, sheetName: String): (Worksheet, Seq[Row]) = {
    val futureRows =
      for {
        acct <- Account(username, password)
        ss <- acct.spreadsheets
        ws <- ss("Example Spreadsheet").worksheets
        sheet = ws(sheetName)
        rows <- sheet.rows
      } yield (sheet, rows)

    Await.result(futureRows, shortWait)
  }

  def fetchSheetAndCells(username: String, password: String, sheetName: String): (Worksheet, Seq[Cell]) = {
    val futureCells =
      for {
        acct <- Account(username, password)
        ss <- acct.spreadsheets
        ws <- ss("Example Spreadsheet").worksheets
        sheet = ws(sheetName)
        cells <- sheet.cells
      } yield (sheet, cells)

    Await.result(futureCells, shortWait)
  }
}
