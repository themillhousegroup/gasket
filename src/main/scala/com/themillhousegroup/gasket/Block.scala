package com.themillhousegroup.gasket

import scala.concurrent.Future

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
}
