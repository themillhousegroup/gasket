package com.themillhousegroup.gasket.traits

import com.google.gdata.data.BaseEntry

trait ScalaEntry[T <: BaseEntry[T]] {
  /** Access to the underlying Google *Entry object */
  val googleEntry: T

  lazy val title = googleEntry.getTitle.getPlainText

  override def toString = title
}
