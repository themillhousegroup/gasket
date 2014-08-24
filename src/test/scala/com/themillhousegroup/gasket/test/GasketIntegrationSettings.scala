package com.themillhousegroup.gasket.test

import scala.concurrent.duration.Duration
import java.io.File


/**
 * Expects to be able to find a file at:
 *
 *    ~/.gasket/.credentials
 *
 * Consisting of exactly two lines:
 *
 * <google-username>
 * <app-password>
 *
 * Where <google-username> should be something like bob@gmail.com
 * and the <app-password> should (ideally) be an "App Password" that you've created
 * just for Gasket by using a browser to log in to Google, then going to
 * Account -> Security -> App Passwords -> Settings
 */
trait GasketIntegrationSettings {
  lazy val timeout = Duration(10, "seconds")

  lazy val homeDir = System.getProperty("user.home")
  lazy val credentialsFile = new File(s"$homeDir/.gasket", ".credentials")

  lazy val credentialsIterator = scala.io.Source.fromFile(credentialsFile).getLines()

  private def readLine(target: String) = {
    if (!credentialsIterator.hasNext) {
      throw new IllegalStateException(s"couldn't find a line for $target")
    }

    credentialsIterator.next
  }

  lazy val username = readLine("username")
  lazy val password = readLine("password")

}
