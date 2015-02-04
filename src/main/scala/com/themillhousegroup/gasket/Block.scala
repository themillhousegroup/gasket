package com.themillhousegroup.gasket

case class Block(rowNumber: Int, cells: Seq[Cell]) extends Ordered[Block] {

  def compare(that: Block): Int = this.rowNumber - that.rowNumber
}
