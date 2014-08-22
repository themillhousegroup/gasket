package com.themillhousegroup.gasket.traits

import com.google.gdata.data.BaseEntry

trait ScalaEntry[T <: BaseEntry[T]] {
  protected val entry: T

  lazy val title = entry.getTitle.getPlainText

  override def toString = title
}
