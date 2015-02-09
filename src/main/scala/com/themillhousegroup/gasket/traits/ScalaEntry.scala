package com.themillhousegroup.gasket.traits

import com.google.gdata.data.BaseEntry

trait ScalaEntry[T <: BaseEntry[T]] {
  /** Access to the underlying Google *Entry object */
  val googleEntry: T

  lazy val title = googleEntry.getTitle.getPlainText

  override def toString = title

  // Avoid precondition-failed problems based on the resource versioning used by Google:
  //http://stackoverflow.com/questions/19006892/com-google-gdata-util-preconditionfailedexception-on-listentry-update-in-googl
  googleEntry.setEtag("*")
}
