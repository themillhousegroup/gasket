package com.themillhousegroup.gasket

import scala.concurrent.Future

/**
 * Represents a rectangular area of a Worksheet that has already
 * been fetched from the remote API.
 *
 * Operations on a Block will be performed using the Google API's
 * batching mechanism for improved performance over single-Cell updates.
 */
case class Block(parent:Worksheet, cells: Seq[Cell]) extends Ordered[Block] {

  lazy val minRow = cells.head.rowNumber
  lazy val minColumn = cells.head.colNumber

  lazy val width =  cells.last.colNumber - minColumn
  lazy val height =  cells.last.rowNumber - minRow

  def compare(that: Block): Int = this.minRow - that.minRow

  /** A new value must be provided for each cell in the block,
    * even if it's unchanged. The length of `newValues` must be
    * the same as the original `cells` sequence.
    */
  def update(newValues:Seq[String]):Future[Block] = {
    Future.successful(this)
  }

  def rows:Seq[Row] = {
    cells.grouped(width).map { cRow =>
      Row(cRow.head.rowNumber, cRow)
    }.toSeq
  }
}
