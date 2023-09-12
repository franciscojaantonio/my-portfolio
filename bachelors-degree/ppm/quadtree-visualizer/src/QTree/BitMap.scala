package QTree

import QTreeUtils._
import scala.annotation.tailrec

case class BitMap[A](points: List[List[A]]) {

  def toQuad: (List[List[A]], List[List[A]], List[List[A]], List[List[A]]) = BitMap.toQuad(points)
  def getLength: (Int, Int) = BitMap.getLength(points)
  /* Métodos não utilizados
  def rotateR:BitMap[A] = BitMap(BitMap.rot_r(points))
  def rotateL:BitMap[A] = BitMap(BitMap.rot_l(points))
  def mirrorV:BitMap[A] = BitMap(BitMap.mirror_v(points))
  def mirrorH:BitMap[A] = BitMap(BitMap.mirror_h(points))
  */
}

object BitMap {

  def createBitMapInt(imgPath: String): BitMap[Int] = {
    val imgData = ImageUtil.readColorImage(imgPath)
    new BitMap(arrayOfArrayToListOfList(imgData))
  }

  def makeBitmap(qt: QuadTree): BitMap[Int] = {
    qt.n match {
      case n: QLeaf[Coords, Section] => leafToBitMap(n)
      case n: QNode[Coords] => nodeToBitMap(n)
      case QEmpty => new BitMap(List[List[Int]]())
    }
  }

  def getLength(points: List[List[Any]]): (Int, Int) = {
    (points.head.length, points.length)
  }

  def div_h[A](points: List[List[A]]): (List[List[A]], List[List[A]]) = {
    val le = points.length
    if (le % 2 == 0) points.splitAt(le / 2)
    else points.splitAt(le / 2 + 1)
  }

  def div_v[A](points: List[List[A]]): (List[List[A]], List[List[A]]) = {
    @tailrec
    def aux(l: List[List[A]], r: List[List[A]], li: List[List[A]]): (List[List[A]], List[List[A]]) = {
      li match {
        case Nil => (l, r)
        case x :: xs =>
          val le = li.head.length
          val (lHalf, rHalf) = if (le % 2 == 0) x.splitAt(le / 2) else x.splitAt(le / 2 + 1)
          if (rHalf.isEmpty) aux(l :+ lHalf, r, xs)
          else aux(l :+ lHalf, r :+ rHalf, xs)
      }
    }
    aux(List[List[A]](), List[List[A]](), points)
  }

  def toQuad[A](points: List[List[A]]): (List[List[A]], List[List[A]], List[List[A]], List[List[A]]) = {
    val tup = div_v(points)
    val tup1 = div_h(tup._1)
    val tup2 = div_h(tup._2)
    (tup1._1, tup2._1, tup1._2, tup2._2)
  }

  def join_h[A](tup: (List[List[A]], List[List[A]])): List[List[A]] = {
    tup._1 ::: tup._2
  }

  def join_v[A](tup: (List[List[A]], List[List[A]])): List[List[A]] = {
    @tailrec
    def aux(t: (List[List[A]], List[List[A]]), l: List[List[A]]): List[List[A]] = {
      t match {
        case (Nil, l2) => l ::: l2
        case (l1, Nil) => l ::: l1
        case (x :: xs, y :: ys) => aux((xs, ys), l :+ (x ::: y))
      }
    }
    aux(tup, List[List[A]]())
  }

  /*
  def mirror_h[A](points: List[List[A]]): List[List[A]] = {
    if (points.length <= 1) points
    else {
      val (t, bt) = div_h(points)
      join_h(mirror_h(bt), mirror_h(t))
    }
  }

  def mirror_v[A](points: List[List[A]]): List[List[A]] = {
    if (points.head.length <= 1) points
    else {
      val (l, r) = div_v(points)
      join_v(mirror_v(r), mirror_v(l))
    }
  }

  def rot_r[A](points: List[List[A]]): List[List[A]] = {
    @tailrec
    def aux(l: List[List[A]], l2: List[List[A]]): List[List[A]] = {
      l match {
        case Nil => l2
        case x :: xs => aux(xs, join_v(x.foldRight(List[List[A]]())(List[A](_) :: _), l2))
      }
    }
    aux(points, List[List[A]]())
  }

  def rot_l[A](points: List[List[A]]): List[List[A]] = {
    @tailrec
    def aux(l: List[List[A]], l2: List[List[A]]): List[List[A]] = {
      l match {
        case Nil => l2
        case x :: xs => aux(xs, join_v(l2, x.foldLeft(List[List[A]]())((xs, x) => List[A](x) :: xs)))
      }
    }
    aux(points, List[List[A]]())
  }
  */
}