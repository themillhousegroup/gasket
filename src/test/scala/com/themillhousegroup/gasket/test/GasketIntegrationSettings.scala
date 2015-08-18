package com.themillhousegroup.gasket.test

import scala.concurrent.duration.Duration
import java.io.File
import org.specs2.specification.Scope
import org.specs2.execute.{ StandardResults, ResultLike, Result }
import org.slf4j.LoggerFactory
import com.themillhousegroup.gasket.{ Account, Row, Worksheet }
import scala.concurrent.Await

/**
 * Expects to be able to find a file at:
 *
 *    ~/.gasket/.credentials
 *
 * Consisting of exactly two lines:
 *
 * <google-clientid>
 * <p12-file-location>
 *
 * Where <google-clientid> should be something like abc-123-def-456@developer.gserviceaccount.com
 * and the <p12-file-location> should be the absolute path to a .p12 file containing credentials
 * for this Service Account
 */
trait GasketIntegrationSettings {
  lazy val homeDir = System.getProperty("user.home")
  lazy val credentialsFile = new File(s"$homeDir/.gasket", ".credentials")
  lazy val maybeCredentials = if (credentialsFile.exists()) Some(credentialsFile) else None

  private def withFileIterator[T](f: File)(b: Iterator[String] => T): T = {
    val src = scala.io.Source.fromFile(f)
    implicit val credentialsIterator = src.getLines()

    val t = b(credentialsIterator)
    src.close
    t
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

    def apply(block: (String, File) => Result): Scope = {
      maybeCredentials.fold {
        new Scope with StandardResults {
          log.warn(skipMessage)
          skipped(skipMessage)
        }.asInstanceOf[Scope]
      } {
        withFileIterator(_) { implicit it =>

          lazy val clientId = readLine("clientId")
          lazy val p12FileLocation = readLine("p12-file-location")

          new Scope {
            block(clientId, new java.io.File(p12FileLocation))
          }
        }

      }
    }
  }
}

