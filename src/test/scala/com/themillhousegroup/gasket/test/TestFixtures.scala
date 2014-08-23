package com.themillhousegroup.gasket.test

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet._
import java.net.URL
import com.google.gdata.data.PlainTextConstruct
import org.specs2.mock.Mockito
import com.google.gdata.client.GoogleService.InvalidCredentialsException

trait TestFixtures {
  this: Mockito =>

  val fakeUrl = new URL("http://localhost")

  val mockService = mock[SpreadsheetService]

  val s1 = mock[SpreadsheetEntry]
  val s2 = mock[SpreadsheetEntry]
  s1.getTitle returns new PlainTextConstruct("one")
  s2.getTitle returns new PlainTextConstruct("two")

  val mockSpreadsheetEntry = mock[SpreadsheetEntry]
  mockSpreadsheetEntry.getWorksheetFeedUrl returns fakeUrl

  val mockWorksheetFeed = mock[WorksheetFeed]
  val w1 = mock[WorksheetEntry]
  val w2 = mock[WorksheetEntry]
  w1.getTitle returns new PlainTextConstruct("one")
  w2.getTitle returns new PlainTextConstruct("two")

  mockWorksheetFeed.getEntries returns com.google.common.collect.Lists.newArrayList(w1, w2)

  val mockWorksheetEntry = mock[WorksheetEntry]
  mockWorksheetEntry.getCellFeedUrl returns fakeUrl

  val mockCellFeed = mock[CellFeed]
  val c1 = mock[CellEntry]
  val c2 = mock[CellEntry]
  c1.getTitle returns new PlainTextConstruct("one")
  c2.getTitle returns new PlainTextConstruct("two")
  mockCellFeed.getEntries returns com.google.common.collect.Lists.newArrayList(c1, c2)

}
