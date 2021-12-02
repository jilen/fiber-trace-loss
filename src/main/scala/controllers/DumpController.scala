package controllers

import cats.syntax.all._
import cats.effect._
import cats.effect.syntax.all._
import java.lang.management.ManagementFactory
import play.api.mvc._
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class DumpController(
  Action: FActionBuilder.Default[IO]) {

  def peoridicDump =  {
    obtainDump.delayBy(3.seconds).flatMap(IO.println).foreverM
  }

  private def obtainDump = {
    IO.delay {
      val server = ManagementFactory.getPlatformMBeanServer()
      val beans  = server.queryMBeans(null, null).asScala.toSeq

      val objs = beans.collect {
        case b
            if b.getObjectName().toString.contains("LiveFiberSnapshotTrigger") =>
          b.getObjectName()
      }
     objs.flatMap { obj =>
        server
          .invoke(
            obj,
            "liveFiberSnapshot",
            Array.empty[AnyRef],
            Array.empty[String]
          )
          .asInstanceOf[Array[String]]
      }.mkString
    }
  }

  def dumpFibers = Action { (_) =>
    obtainDump.map(Results.Ok(_))
  }
}
