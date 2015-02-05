package com.themillhousegroup.gasket

case class Block(cells: Seq[Cell]) extends Ordered[Block] {

  lazy val minRow = cells.min.rowNumber

  def compare(that: Block): Int = this.minRow - that.minRow
}
