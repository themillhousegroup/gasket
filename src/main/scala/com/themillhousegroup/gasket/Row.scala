package com.themillhousegroup.gasket

/** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
case class Row(rowNumber: Int, cells: Seq[Cell]) extends Ordered[Row] {

  def compare(that: Row): Int = this.rowNumber - that.rowNumber

}
