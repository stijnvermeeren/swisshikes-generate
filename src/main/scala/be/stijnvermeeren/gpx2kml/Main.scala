package be.stijnvermeeren.gpx2kml

import com.typesafe.config.ConfigFactory

object Main extends App {
  val config = ConfigFactory.load().getConfig("be.stijnvermeeren.gpx2kml")

  val inDirectory = config.getString("inDirectory")
  val outPath = config.getString("outPath")
  val title = config.getString("title")
  val lineColor = config.getString("lineColor")
  val lineWidth = config.getInt("lineWidth")

  println(s"Reading from directory $inDirectory and writing to $outPath.")

  Gpx2Kml.convert(
    inDirectory = inDirectory,
    outPath = outPath,
    title = title,
    lineColor = lineColor,
    lineWidth = lineWidth
  )
}