package com.themillhousegroup.gasket

/** Cells are what actually make up the Worksheet; Rows are basically a view onto them */
case class Row(rowNumber: Int, cells: Seq[Cell]) extends Ordered[Row] {

  lazy val matchChars = """([a-zA-Z]*)(\d*)""".r

  def compare(that: Row): Int = this.rowNumber - that.rowNumber

  /**
   * Because the Google API omits blank cells,
   * the `cells` Seq in this row might not be lined up
   * with the columns in the worksheet - i.e. cells(3) might not actually be
   * the fourth column in the sheet.
   * This function provides a way to access the cells by the "R1C1-style" 1-based column number
   * @param column
   */
  def cellAt(column: Int): Option[Cell] = {
    cells.find(_.colNumber == column)
  }

  /**
   * Because the Google API omits blank cells,
   * the `cells` Seq in this row might not be lined up
   * with the columns in the worksheet - i.e. cells(3) might not actually be
   * the fourth column in the sheet.
   * This function provides a way to access the cells by the "A1-style" alphabetical column name
   * @param columnName
   */
  def cellAt(columnName: String): Option[Cell] = {
    cells.find { cell =>
      val columnTitle = cell.googleEntry.getTitle.getPlainText
      columnTitle match {
        case matchChars(colName, _) => colName == columnName
      }
    }
  }

  /**
   * Direct access to the contents of the Cells that make up this Row.
   * Note that due to the Google API omitting empty cells, this might
   * not "line up" with the visual appearance of the row.
   */
  def values: Seq[String] = cells.map(_.value)

  /**
   * Direct access to the contents of the Cells that make up this Row.
   * Note that due to the Google API omitting empty cells, this might
   * not "line up" with the visual appearance of the row.
   *
   * @return a seq of (columnNumber, value) tuples
   */
  def columnValues: Seq[(Int, String)] = cells.map(cell => (cell.colNumber -> cell.value))

}
