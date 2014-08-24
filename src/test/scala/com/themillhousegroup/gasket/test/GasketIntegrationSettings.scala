package com.themillhousegroup.gasket.test

import scala.concurrent.duration.Duration
import java.io.File

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
