package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.traits.ScalaEntry
import com.google.gdata.data.spreadsheet.CellEntry
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class Cell(parent: Worksheet, val googleEntry: CellEntry) extends ScalaEntry[CellEntry] with Ordered[Cell] {
  lazy val value = googleEntry.getCell.getValue
  lazy val numericValue = googleEntry.getCell.getNumericValue

  lazy val rowNumber = googleEntry.getCell.getRow
  lazy val colNumber = googleEntry.getCell.getCol

  override def toString = s"[R${rowNumber}C${colNumber}]: $value"

  def compare(that: Cell): Int = this.colNumber - that.colNumber

  /**
   * THIS WILL NOT MUTATE THE CURRENT OBJECT!
   *
   * If newValue != value, pushes the change back to the source worksheet.
   * @return a Future holding a Cell containing the updated value
   */
  def update(newValue: String): Future[Cell] = {
    if (newValue == value) {
      Future.successful(this)
    } else {
      Future {
        val newEntry = new CellEntry(rowNumber, colNumber, newValue)
        val fromRemote = newEntry.update // Blocks
        Cell(parent, fromRemote)
      }
    }
  }
}
