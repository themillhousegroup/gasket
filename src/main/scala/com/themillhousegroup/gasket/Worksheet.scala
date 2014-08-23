package com.themillhousegroup.gasket

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet.{ CellFeed, ListEntry, ListFeed, WorksheetEntry }
import com.themillhousegroup.gasket.traits.ScalaEntry
import java.net.URI
import com.google.common.base.Stopwatch

case class Worksheet(private val service: SpreadsheetService, val parent: Spreadsheet, val googleEntry: WorksheetEntry) extends ScalaEntry[WorksheetEntry] {

  val cellFeedBaseUrl = new URI(googleEntry.getCellFeedUrl.toString).toURL
  // + "?min-row=2&min-col=4&max-col=4").toURL();

  lazy val cellFeed = service.getFeed(cellFeedBaseUrl, classOf[CellFeed])

  private def asRowSeq(jResults: java.util.List[ListEntry]): Seq[Row] = {
    import scala.collection.JavaConverters._
    jResults.asScala.map(Row(this, _))
  }

  def rows: Seq[Row] = {

    val totalRows = googleEntry.getRowCount

    fetchSubsequentRows(Seq(), totalRows)
  }

  private def fetchSubsequentRows(acc: Seq[Row], totalRows: Int): Seq[Row] = {
    val highestRowSoFar = acc.size
    val selectedFeedUrl = new URI(s"${googleEntry.getListFeedUrl().toString()}?min-row=$highestRowSoFar").toURL
    val selectedFeed = service.getFeed(selectedFeedUrl, classOf[ListFeed])

    val s = new Stopwatch()
    s.start
    val nextBatch = asRowSeq(selectedFeed.getEntries)
    s.stop
    println(s"Row fetch took ${s.elapsedMillis}ms")

    val result = acc ++ nextBatch

    if (result.size < totalRows) {
      fetchSubsequentRows(result, totalRows)
    } else {
      result
    }
  }

  def cells: Seq[Cell] = {
    import scala.collection.JavaConverters._

    cellFeed.getEntries.asScala.map(Cell(this, _))
  }
}
