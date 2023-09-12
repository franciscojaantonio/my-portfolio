package QTree

import BitMap.{join_h, join_v, makeBitmap}

import java.awt.Color
import java.util.Random
import scala.annotation.tailrec
import scala.util.Random.javaRandomToRandom

object QTreeUtils {

  type Point = (Int, Int)
  type Coords = (Point, Point)
  type Section = (Coords, Color)

  trait QTree[+A]

  case class QNode[A](value: A,
                      one: QTree[A], two: QTree[A],
                      three: QTree[A], four: QTree[A]) extends QTree[A]

  case class QLeaf[A, B](value: B) extends QTree[A]

  case object QEmpty extends QTree[Nothing]

  def add(p1: Point, p2: Point): Point = {
    (p1._1 + p2._1, p1._2 + p2._2)
  }

  def halfSize(point: Point): Point = {
    val (x, y) = point
    (math.round(x / 2f), math.round(y / 2f))
  }

  def arrayOfArrayToListOfList[A](x: Array[Array[A]]): List[List[A]] = {
    x.map(_.toList).toList
  }

  def listOfListToArrayOfArray(l: List[List[Int]]): Array[Array[Int]] = {
    l.map(_.toArray).toArray
  }

  def nodeToBitMap(n: QNode[Coords]): BitMap[Int] = {
    BitMap(join_h(join_v(treeToBitMap(n.one).points, treeToBitMap(n.two).points),
      join_v(treeToBitMap(n.three).points, treeToBitMap(n.four).points)))
  }

  def treeToBitMap(qt: QTree[Coords]): BitMap[Int] = {
    qt match {
      case n: QLeaf[Coords, Section] => leafToBitMap(n)
      case n: QNode[Coords] => nodeToBitMap(n)
      case _: QTree[Nothing] => BitMap[Int](List[List[Int]]())
    }
  }

  def leafToBitMap(f: QLeaf[Coords, Section]): BitMap[Int] = {
    val c = f.value._1
    val p = (c._2._1 - c._1._1, c._2._2 - c._1._2)

    @tailrec
    def aux(p: Point, l: List[List[Int]]): List[List[Int]] = {
      p match {
        case (0, _) => l
        case (_, 0) => l
        case (x, y) => aux((x, y - 1), l :+ (1 to x).map(_ => f.value._2.getRGB).toList)
      }
    }
    BitMap[Int](aux(p, List[List[Int]]()))
  }

  def mirrorCoordsV(c: Coords, x: Int): Coords = {
    ((x - c._2._1, c._1._2), (x - c._1._1, c._2._2))
  }

  def mirrorCoordsH(c: Coords, y: Int): Coords = {
    ((c._1._1, y - c._2._2), (c._2._1, y - c._1._2))
  }

  def rotCoordsL(c: Coords, x: Int): Coords = {
    ((c._1._2, x - c._2._1), (c._2._2, x - c._1._1))
  }

  def rotCoordsR(c: Coords, y: Int): Coords = {
    ((y - c._2._2, c._1._1), (y - c._1._2, c._2._1))
  }

  def getCoordsLength(c: Coords): Point = {
    val ((x1, y1), (x2, y2)) = c
    (x2 - x1, y2 - y1)
  }

  def Contrast(color: Color): Color = {
    val contrast_value = 0.6
    val brightness = 0
    val c = ImageUtil.decodeRgb(color.getRGB)
    val r = (((contrast_value * (c(0) - 128)) + 128 + brightness) + 0.5).toInt min 255
    val g = (((contrast_value * (c(1) - 128)) + 128 + brightness) + 0.5).toInt min 255
    val b = (((contrast_value * (c(2) - 128)) + 128 + brightness) + 0.5).toInt min 255
    new Color(r,g,b)
  }

  /*
  def contrast(c: Color): Color = {
    val y = (299 * c.getRed + 587 * c.getGreen + 114 * c.getBlue) / 1000f
    if (y >= 128f)
      c.darker()
    else
      c.brighter()
  }
  */

  def Noise_1(color: Color, i: Int, j: Int): Color = {
    val c = ImageUtil.decodeRgb(color.getRGB)
    if(i < 1)
      new Color(j, j, j)
    else
      new Color((c.sum/3 + c.head) / 2, (c.sum/3 + c(1)) / 2, (c.sum/3 + c(2)) / 2)
      // color
  }

  def Noise(color: Color): Color = {
    val c = ImageUtil.decodeRgb(color.getRGB)
    val rand = new Random().between(0, 10)
    if(rand < 2){
      val grayValue = new Random().between(0, 255)
      new Color(grayValue, grayValue, grayValue)
    }
    else
      new Color((c.sum/3 + c.head) / 2, (c.sum/3 + c(1)) / 2, (c.sum/3 + c(2)) / 2)
      // color
  }

  def Sepia(c: Color): Color = {
    val (r, g, b) = (c.getRed, c.getGreen, c.getBlue)
    val newRed = scala.math.min(0.393 * r + 0.769 * g + 0.189 * b, 255).toInt
    val newGreen = scala.math.min(0.349 * r + 0.686 * g + 0.168 * b, 255).toInt
    val newBlue = scala.math.min(0.272 * r + 0.534 * g + 0.131 * b, 255).toInt
    new Color(newRed, newGreen, newBlue)
  }

  def writeQuadTree(qt: QTree[Coords], path: String, format: String): Unit = {
    val bitmap = makeBitmap(new QuadTree(qt))
    ImageUtil.writeImage(listOfListToArrayOfArray(bitmap.points), path, format)
  }

  def prepareNewCords(coords: Coords): Option[(Coords, Coords, Coords, Coords)] = {
    val size = getCoordsLength(coords)
    if ((size._1 <= 1 && size._2 <= 1) || size._1 == 0 || size._2 == 0) return None
    val width = math.round(size._1 / 2f)
    val height = math.round(size._2 / 2f)
    val one = (add((0, 0), coords._1), add((width, height), coords._1))
    val two = (add((width, 0), coords._1), add((size._1, height), coords._1))
    val three = (add((0, height), coords._1), add((width, size._2), coords._1))
    val four = (add((width, height), coords._1), add(size, coords._1))
    Some(one, two, three, four)
  }
}