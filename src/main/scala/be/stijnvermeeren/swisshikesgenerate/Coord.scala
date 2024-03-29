package be.stijnvermeeren.swisshikesgenerate


case class Coord(lon: Double, lat: Double) {
  override def toString: String = {
    s"${Coord.formatPart(lon)},${Coord.formatPart(lat)}"
  }
}

object Coord {
  def formatPart(value: Double): String = {
    val output = f"$value%.5f" // five decimal places after the point
    output.reverse.dropWhile(_ == '0').dropWhile(_ == '.').reverse // drop zeroes at the end
  }
}
