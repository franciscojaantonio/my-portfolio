package QTree

import QTreeUtils._
import Random._
import java.awt.Color

case class QuadTree(n: QTree[Coords]) {

  override def toString: String = n.toString
  def scale(factor: Double): QuadTree = QuadTree(QuadTree.scale(factor, n))
  def mirrorV: QuadTree = QuadTree(QuadTree.mirrorV(n))
  def mirrorH: QuadTree = QuadTree(QuadTree.mirrorH(n))
  def rotateL: QuadTree = QuadTree(QuadTree.rotateL(n))
  def rotateR: QuadTree = QuadTree(QuadTree.rotateR(n))
  def mapColorEffect(f: Color => Color): QuadTree = QuadTree(QuadTree.mapColourEffect(f, n))
  def mapColorEffect_1(f: (Color, Int, Int) => Color, seed: Int): QuadTree = QuadTree(QuadTree.mapColourEffect_1(f, MyRandom(seed), n))
  def getQuadLength: Point = QuadTree.getQuadLength(n)

}

object QuadTree {

  def getQuadLength(qt: QTree[Coords]): Point = {
    qt match {
      case QLeaf(value: Section) => add(value._1._2, (-value._1._1._1, -value._1._1._2))
      case QNode(value, _, _, _, _) => add(value._2, (-value._1._1, -value._1._2))
      case QEmpty => (0, 0)
    }
  }

  def makeQTree(b: BitMap[Int]): QTree[Coords] = {
    def aux(list: List[List[Int]], p: Point): QTree[Coords] = {
      val l = if (list.isEmpty) (0, 0) else (list.head.size, list.size)
      val hl = halfSize(l)
      val c = (p, add(l, p))
      val (l1, l2, l3, l4) = BitMap.toQuad(list)
      l match {
        case (0, 0) => QEmpty
        case (1, 1) => QLeaf[Coords, Section](c, Color.decode(list.head.head.toString))
        case (_, _) => val trees = (aux(l1, p), aux(l2, add((hl._1, 0), p)), aux(l3, add((0, hl._2), p)), aux(l4, add(hl, p)))
          trees match {
            case (QLeaf((_, col1: Color)), QLeaf((_, col2: Color)), QLeaf((_, col3: Color)), QLeaf((_, col4: Color)))
              if col1.equals(col2) && col1.equals(col3) && col1.equals(col4) => QLeaf(c, col1)
            case (QLeaf((_, col1: Color)), QEmpty, QLeaf((_, col3: Color)), QEmpty)
              if col1.equals(col3) => QLeaf(c, col1)
            case (QLeaf((_, col1: Color)), QLeaf((_, col2: Color)), QEmpty, QEmpty)
              if col1.equals(col2) => QLeaf(c, col1)
            case (t1, t2, t3, t4) => QNode[Coords](c, t1, t2, t3, t4)
          }
      }
    }
    aux(b.points, (0, 0))
  }

  def mirrorV(qt: QTree[Coords]): QTree[Coords] = {
    def aux(q: QTree[Coords], x: Int): QTree[Coords] = {
      q match {
        case QLeaf(value: Section) => QLeaf[Coords, Section](mirrorCoordsV(value._1, x), value._2)
        case QNode(value, one, two, three, four) =>
          QNode[Coords](mirrorCoordsV(value, x), aux(two, x), aux(one, x), aux(four, x), aux(three, x))
        case QEmpty => q
      }
    }
    qt match {
      case QLeaf(_: Section) => qt
      case n: QNode[Coords] => aux(n, n.value._2._1)
      case QEmpty => qt
    }
  }

  def mirrorH(qt: QTree[Coords]): QTree[Coords] = {
    def aux(q: QTree[Coords], y: Int): QTree[Coords] = {
      q match {
        case QLeaf(value: Section) => QLeaf[Coords, Section](mirrorCoordsH(value._1, y), value._2)
        case QNode(value, one, two, three, four) =>
          QNode[Coords](mirrorCoordsH(value, y), aux(three, y), aux(four, y), aux(one, y), aux(two, y))
        case QEmpty => q
      }
    }
    qt match {
      case QLeaf(_: Section) => qt
      case n: QNode[Coords] => aux(n, n.value._2._2)
      case QEmpty => qt
    }
  }

  def rotateL(qt: QTree[Coords]): QTree[Coords] = {
    def aux(q: QTree[Coords], x: Int): QTree[Coords] = {
      q match {
        case QLeaf(value: Section) => QLeaf[Coords, Section](rotCoordsL(value._1, x), value._2)
        case QNode(value, one, two, three, four) =>
          QNode[Coords](rotCoordsL(value, x), aux(two, x), aux(four, x), aux(one, x), aux(three, x))
        case QEmpty => q
      }
    }
    qt match {
      case QLeaf(value: Section) => QLeaf[Coords, Section](rotCoordsL(value._1, value._1._2._1), value._2)
      case n: QNode[Coords] => aux(n, n.value._2._1)
      case QEmpty => qt
    }
  }

  def rotateR(qt: QTree[Coords]): QTree[Coords] = {
    def aux(q: QTree[Coords], y: Int): QTree[Coords] = {
      q match {
        case QLeaf(value: Section) => QLeaf[Coords, Section](rotCoordsR(value._1, y), value._2)
        case QNode(value, one, two, three, four) =>
          QNode[Coords](rotCoordsR(value, y), aux(three, y), aux(one, y), aux(four, y), aux(two, y))
        case QEmpty => q
      }
    }
    qt match {
      case QLeaf(value: Section) => QLeaf[Coords, Section](rotCoordsR(value._1, value._1._2._2), value._2)
      case n: QNode[Coords] => aux(n, n.value._2._2)
      case QEmpty => qt
    }
  }

  def mapColourEffect(f: Color => Color, qt: QTree[Coords]): QTree[Coords] = {
    qt match {
      case QLeaf(value: Section) => QLeaf[Coords, Section](value._1, f(value._2))
      case QNode(value, one, two, three, four) => QNode[Coords](value, mapColourEffect(f, one), mapColourEffect(f, two), mapColourEffect(f, three), mapColourEffect(f, four))
      case QEmpty => qt
    }
  }

  def mapColourEffect_1(f: (Color, Int, Int) => Color, random: RandomWithState, qt: QTree[Coords]): QTree[Coords] = {
    val (i, r) = random.nextInt(10)
    val (i1, r1) = r.nextInt(255)
    val (i2, r2) = r1.nextInt(200)
    qt match {
      case QLeaf(value: Section) => QLeaf[Coords, Section](value._1, f(value._2, i, i1))
      case QNode(value, one, two, three, four) => QNode[Coords](value, mapColourEffect_1(f, r1, one), mapColourEffect_1(f, MyRandom(i2), two),
        mapColourEffect_1(f, r2, three), mapColourEffect_1(f, MyRandom(i1), four))
      case QEmpty => qt
    }
  }

  def blend(l: List[Color]): Color = {
    val r = 1f / l.size
    val red = l.map(_.getRed).foldRight(0f)(_ * r + _)
    val green = l.map(_.getGreen).foldRight(0f)(_ * r + _)
    val blue = l.map(_.getBlue).foldRight(0f)(_ * r + _)
    new Color(red.toInt, green.toInt, blue.toInt)
  }

  def meanColor(qt: QTree[Coords]): Color = {
    def isQEmpty(t: QTree[Coords]): Boolean = {
      t match {
        case QEmpty => true
        case _ => false
      }
    }
    qt match {
      case QEmpty => Color.black
      case QLeaf(v: Section) => v._2
      case QNode(_, one, two, three, four) =>
        blend(List(one, two, three, four).filterNot(isQEmpty).map(meanColor))
    }
  }

  def scale(factor: Double, qt: QTree[Coords]): QTree[Coords] = {
    if (factor <= 0) {
      println("Invalid factor: (" + factor + ")")
      return qt
    }
    val size = getQuadLength(qt)
    val scaledSize = (((size._1 * factor) + 0.5).toInt, ((size._2 * factor) + 0.5).toInt)
    val iniCoords = ((0, 0), scaledSize)
    def aux(q: QTree[Coords], coords: Coords): QTree[Coords] = {
      q match {
        case QLeaf(v: Section) => QLeaf[Coords, Section](coords, v._2)
        case QEmpty => QEmpty
        case QNode(_, one, two, three, four) =>
          prepareNewCords(coords) match {
            case None => QLeaf[Coords, Section](coords, meanColor(q))
            case Some((c1, c2, c3, c4)) =>
              (one, two, three, four) match {
                case (_, _, QEmpty, QEmpty) =>
                  QNode(coords, aux(one, c1), aux(two, c2), aux(one, c3), aux(two, c4))
                case (_, QEmpty, _, QEmpty) =>
                  QNode(coords, aux(one, c1), aux(one, c2), aux(three, c3), aux(three, c4))
                case _ => QNode(coords, aux(one, c1), aux(two, c2), aux(three, c3), aux(four, c4))
              }
          }
      }
    }
    aux(qt, iniCoords)
  }
}