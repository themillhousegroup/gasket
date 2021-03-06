package com.themillhousegroup.gasket

import com.themillhousegroup.gasket.traits.ScalaEntry
import com.google.gdata.data.spreadsheet.CellEntry
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/** The essence of a Cell without the Google-specifics */
trait EssentialCell {

  val value: String
  val numericValue: Number

  /** A Cell's value is considered to be a None if it is empty or entirely whitespace */
  lazy val valueOption = if (value.trim.isEmpty) None else Some(value)

  /** A Cell's value is considered to be a None if it is empty or entirely whitespace */
  lazy val numericValueOption = valueOption.map(_ => numericValue)

  val rowNumber: Int
  val colNumber: Int

  /** Useful for batched operations; see BatchSender.scala */
  private[gasket] lazy val idString = s"R${rowNumber}C${colNumber}"

  override def toString = s"[$idString]: $value"

}

case class Cell(parent: Worksheet,
    val googleEntry: CellEntry,
    cellEntryCopyConstructor: (CellEntry) => CellEntry = Cell.copyConstructor) extends ScalaEntry[CellEntry] with Ordered[Cell] with EssentialCell {
  lazy val value = googleEntry.getCell.getValue

  lazy val numericValue = googleEntry.getCell.getNumericValue

  lazy val rowNumber = googleEntry.getCell.getRow
  lazy val colNumber = googleEntry.getCell.getCol

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
        val newEntry = cellEntryCopyConstructor(googleEntry)
        newEntry.changeInputValueLocal(newValue)
        val fromRemote = newEntry.update // Blocks
        copy(googleEntry = fromRemote)
      }
    }
  }
}

object Cell {
  def copyConstructor(ce: CellEntry): CellEntry = new CellEntry(ce)
}
