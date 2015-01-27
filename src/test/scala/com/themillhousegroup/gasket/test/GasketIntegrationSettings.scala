package com.themillhousegroup.gasket.test

import scala.concurrent.duration.Duration
import java.io.File
import org.specs2.specification.Scope
import org.specs2.execute.{ StandardResults, ResultLike, Result }
import org.slf4j.LoggerFactory

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
  lazy val homeDir = System.getProperty("user.home")
  lazy val credentialsFile = new File(s"$homeDir/.gasket4", ".credentials")
  lazy val maybeCredentials = if (credentialsFile.exists()) Some(credentialsFile) else None

  lazy val credentialsIterator = scala.io.Source.fromFile(credentialsFile).getLines()

  private def readLine(target: String) = {
    if (!credentialsIterator.hasNext) {
      throw new IllegalStateException(s"couldn't find a line for $target")
    }

    credentialsIterator.next
  }

  lazy val username = readLine("username")
  lazy val password = readLine("password")

  object IntegrationScope {
    val skipMessage = "No Credentials in filesystem. Skipping integration test."
    val log = LoggerFactory.getLogger(getClass)

    def apply(block: => ResultLike): Scope = {
      maybeCredentials.fold {
        new Scope with StandardResults {
          log.warn(skipMessage)
          skipped(skipMessage)
        }.asInstanceOf[Scope]
      } { _ =>
        new Scope {
          block
        }
      }
    }
  }
}
