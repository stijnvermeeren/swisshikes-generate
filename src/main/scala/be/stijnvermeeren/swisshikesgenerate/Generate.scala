package be.stijnvermeeren.swisshikesgenerate

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.File
import java.nio.file.Files
import scala.collection.immutable.ListMap
import scala.xml.{Elem, PrettyPrinter, XML}

object Generate {
  final case class MetaData(date: Option[String], description: Option[String], albums: Option[List[String]])

  def findCoordinatesFile(files: Seq[File]): Option[(String, String)] = {
    files.find(_.getName.endsWith(".coordinates.txt")).map { coordinatesFile =>
      (Files.readAllLines(coordinatesFile.toPath).toArray.mkString(" "), coordinatesFile.getName)
    }
  }

  def findGpxFile(files: Seq[File], maxPointsPerLine: Int): Option[(String, String)] = {
    files.find(_.getName.endsWith(".gpx")).flatMap { file =>
      val maxPointsEnforcer = new MaxPointsEnforcer(maxPointsPerLine)

      val trackPoints = for (track <- (XML.loadFile(file) \\ "trkseg").headOption) yield {
        track \\ "trkpt"
      }
      val routePoints = for (track <- (XML.loadFile(file) \\ "rte").headOption) yield {
        track \\ "rtept"
      }

      for (gpxPoints <- trackPoints orElse routePoints) yield {
        val coords = for (point <- gpxPoints) yield {
          Coord(
            point.attribute("lon").get.toString.toDouble,
            point.attribute("lat").get.toString.toDouble
          )
        }

        (maxPointsEnforcer.reducePoints(coords) mkString " ", file.getName)
      }

    }
  }

  def xmlFromDir(
    yearDir: File,
    title: String,
    summerLineColor: String,
    winterLineColor: String,
    lineWidth: Int,
    maxPointsPerLine: Int
  ): XmlData = {
    val year = yearDir.getName

    val mapper = new ObjectMapper(new YAMLFactory())
    mapper.registerModule(DefaultScalaModule)
    val metaDataFile = File(yearDir, "metadata.yml")
    val metaData = if (metaDataFile.exists()) {
      mapper.readValue(
        metaDataFile,
        new TypeReference[Map[String, MetaData]] {}
      )
    } else {
      Map.empty
    }

    val data = for {
      (name, files) <- yearDir.listFiles.groupBy(_.getName.split("[\\._]").head).toList.sortBy(_._1)
      (track, fileName) <- findCoordinatesFile(files) orElse findGpxFile(files, maxPointsPerLine)
    } yield {
      val fileMetaData = metaData.get(fileName)

      val forwardTypeMapping = Map(
        "H" -> "hike",
        "WH" -> "winter hike",
        "R" -> "run",
        "SS" -> "snow shoe hike",
        "ST" -> "ski tour",
        "M" -> "mountaineering",
        "C" -> "climb",
        "VF" -> "via ferrata"
      )

      val nameParts = fileName.split("_").toList
      val autoDescription = nameParts(1).split("-").flatMap(forwardTypeMapping.get).mkString(" and ")
      val description = fileMetaData.flatMap(_.description) orElse Some(autoDescription) filter (_.nonEmpty)

      val title = fileMetaData.flatMap(_.date).getOrElse(nameParts.head)

      val albums = fileMetaData.flatMap(_.albums).getOrElse(List.empty)
      val albumsDescription = if (albums.nonEmpty) {
        val links = albums.map(link => s"""<a href="$link" target="_blank">$link</a>""").mkString(", ")
        Some(s"Photos: $links")
      } else {
        None
      }

      val fullDescription = (description.toSeq ++ albumsDescription).mkString("<br /><br />")

      val winterKeywords = Set("ski", "winter", "snow", "snowshoe")
      val isWinter = description.exists(descriptionString => {
        winterKeywords.exists(descriptionString.toLowerCase.split(" ").contains)
      })
      val lineStyle = if (isWinter) "winter" else "summer"

      val placemark = <Placemark>
        <name>{title}</name>
        {if (fullDescription.nonEmpty) <description>{fullDescription}</description> else {}}
        <styleUrl>{lineStyle}</styleUrl>
        <LineString>
          <altitudeMode>clampToGround</altitudeMode>
          <extrude>1</extrude>
          <tessellate>1</tessellate>
          <coordinates>{track}</coordinates>
        </LineString>
      </Placemark>

      XmlData(placemark, nameParts.headOption)
    }

    val kml = <kml xmlns="http://www.opengis.net/kml/2.2">
      <Document>
        <name>{title} - {year}</name>
        <Style id="winter">
          <LineStyle>
            <color>{winterLineColor}</color>
            <width>{lineWidth}</width>
          </LineStyle>
        </Style>
        <Style id="summer">
          <LineStyle>
            <color>{summerLineColor}</color>
            <width>{lineWidth}</width>
          </LineStyle>
        </Style>
        {data.map(_.xml)}
      </Document>
    </kml>

    XmlData(kml, data.map(_.latestDate).max)
  }
}
