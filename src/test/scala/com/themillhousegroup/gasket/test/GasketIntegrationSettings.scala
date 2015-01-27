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
  lazy val credentialsFile = new File(s"$homeDir/.gasket", ".credentials")
  lazy val maybeCredentials = if (credentialsFile.exists()) Some(credentialsFile) else None

  private def withFileIterator[T](f: File)(b: Iterator[String] => T): T = {
    implicit val credentialsIterator = scala.io.Source.fromFile(f).getLines()

    b(credentialsIterator)
  }

  private def readLine(target: String)(implicit credentialsIterator: Iterator[String]) = {
    if (!credentialsIterator.hasNext) {
      throw new IllegalStateException(s"couldn't find a line for $target")
    }

    credentialsIterator.next
  }

  object IntegrationScope {
    val skipMessage = "No Credentials in filesystem. Skipping integration test."
    val log = LoggerFactory.getLogger(getClass)

    def apply(block: (String, String) => Result): Scope = {
      maybeCredentials.fold {
        new Scope with StandardResults {
          log.warn(skipMessage)
          skipped(skipMessage)
        }.asInstanceOf[Scope]
      } {
        withFileIterator(_) { implicit it =>

          lazy val username = readLine("username")
          lazy val password = readLine("password")

          new Scope {
            block(username, password)
          }
        }

      }
    }
  }
}
