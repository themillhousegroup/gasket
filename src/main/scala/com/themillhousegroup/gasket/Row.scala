package com.themillhousegroup.gasket

/** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
case class Row(rowNumber: Int, cells: Seq[Cell]) extends Ordered[Row] {

  def compare(that: Row): Int = this.rowNumber - that.rowNumber

  /** Because the Google API omits blank cells,
    * the `cells` Seq in this row might not be lined up
    * with the columns in the worksheet - i.e. cells(3) might not actually be
    * the fourth column in the sheet.
    * This function provides a way to access the cells "by column"
    * @param column
    */
  def cellAt(column:Int):Option[Cell] = {
    cells.find(_.colNumber == column)
  }
}
