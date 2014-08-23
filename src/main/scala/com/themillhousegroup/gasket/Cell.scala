package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.traits.ScalaEntry
import com.google.gdata.data.spreadsheet.CellEntry

case class Cell(parent: Worksheet, val googleEntry: CellEntry) extends ScalaEntry[CellEntry] {
  lazy val value = googleEntry.getCell.getValue
  lazy val numericValue = googleEntry.getCell.getNumericValue

  lazy val rowNumber = googleEntry.getCell.getRow
  lazy val colNumber = googleEntry.getCell.getCol

  override def toString = s"[R${rowNumber}C${colNumber}]: $value"
}
