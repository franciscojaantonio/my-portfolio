package GUI

import BitMapAlbum.{Directory, Info, Name}
import QTree.QTreeUtils._
import QTree.BitMap
import scala.annotation.tailrec
import scala.collection.immutable.SortedMap

case class BitMapAlbum(nome: String, data: SortedMap[Int, (BitMap[Int], Info)]) {

  def add(directory: => Directory, nome: => Name): BitMapAlbum = BitMapAlbum.add(directory, nome)(this)
  def remove(num: => Int): BitMapAlbum = BitMapAlbum.remove(num)(this)
  def showContent(): BitMapAlbum = BitMapAlbum.showContent()(this)
  def changeOrder(x: => Int, y: => Int): BitMapAlbum = BitMapAlbum.changeOrder(x, y)(this)
  def writeImg(index: Int): Unit = BitMapAlbum.writeImg(index)(this)
  def addFromFiles(bitMap: BitMap[Int], info: Info): BitMapAlbum = BitMapAlbum.addFromFiles(bitMap, info)(this)
  def update(key: Int, data: (BitMap[Int], Info)): BitMapAlbum = BitMapAlbum.update(key, data)(this)
  def addFromFilesNewKey(bitMap: BitMap[Int], info: Info): BitMapAlbum = BitMapAlbum.addFromFilesNewKey(bitMap, info)(this)

}

object BitMapAlbum {

  type Directory = String
  type Name = String
  type Size = (Int, Int)
  type Info = (Directory, Name, Size, Int)

  @tailrec
  def getFirstEmptyKey(list: List[Int], res: Int): Int = {
    list match {
      case Nil => res
      case h :: t => if (h.equals(res)) getFirstEmptyKey(t, res + 1) else res
    }
  }

  def addFromFilesNewKey(b: BitMap[Int], info: Info)(bitMapAlbum: BitMapAlbum): BitMapAlbum = {
    val num = getFirstEmptyKey(bitMapAlbum.data.keys.toList, 0)
    val indexUpdatedInfo = (info._1, info._2, info._3, num)
    GUI.BitMapAlbum(bitMapAlbum.nome, bitMapAlbum.data + (num -> (b, indexUpdatedInfo)))
  }

  def addFromFiles(b: BitMap[Int], info: Info)(bitMapAlbum: BitMapAlbum): BitMapAlbum = {
    val num = info._4
    val indexUpdatedInfo = (info._1, info._2, info._3, info._4)
    GUI.BitMapAlbum(bitMapAlbum.nome, bitMapAlbum.data + (num -> (b, indexUpdatedInfo)))
  }

  def update(key: => Int, data: (BitMap[Int], Info))(bitMapAlbum: BitMapAlbum): BitMapAlbum = {
    BitMapAlbum(bitMapAlbum.nome, bitMapAlbum.data + (key -> data))
  }

  def add(directory: => Directory, name: => Name)(bitMapAlbum: BitMapAlbum): BitMapAlbum = {
    val num = getFirstEmptyKey(bitMapAlbum.data.keys.toList, 0)
    val dir = directory
    def aux(): Option[BitMap[Int]] = {
      try {
        Some(BitMap.createBitMapInt(dir))
      }
      catch {
        case _: Exception => println("No such File on directory!"); None
      }
    }
    val bm = aux()
    if (bm.isEmpty)
      return bitMapAlbum
    val size = bm.get.getLength
    val info = (dir, name, size, num)
    GUI.BitMapAlbum(bitMapAlbum.nome, bitMapAlbum.data + (num -> (bm.get, info)))
  }

  def writeImg(index: Int)(bitMapAlbum: BitMapAlbum): Unit = {
    ImageUtil.writeImage(listOfListToArrayOfArray(bitMapAlbum.data(index)._1.points), "albumFiles/temp.png", "png")
  }

  def remove(num: => Int)(bitMapAlbum: BitMapAlbum): BitMapAlbum = {
    val key = num
    if (bitMapAlbum.data.contains(key)) {
      val aux = BitMapAlbum(bitMapAlbum.nome, bitMapAlbum.data - key)
      val new_data = aux.data.map(x => if (x._1 > num) x._1 - 1 -> (x._2._1, (x._2._2._1, x._2._2._2, x._2._2._3, x._1 - 1)) else x)
      BitMapAlbum(aux.nome, new_data)
    }
    else {
      println("No image with that key!")
      bitMapAlbum
    }
  }

  def changeOrder(x: => Int, y: => Int)(bitMapAlbum: BitMapAlbum): BitMapAlbum = {
    if (bitMapAlbum.data.contains(x) && bitMapAlbum.data.contains(y)) {
      val i = x
      val j = y
      val aux1 = bitMapAlbum.data filter (a => a._1.equals(i))
      val aux2 = bitMapAlbum.data filter (b => b._1.equals(j))
      val new1 = (aux1.head._2._1, aux1.head._2._2.copy(_4 = j))
      val new2 = (aux2.head._2._1, aux2.head._2._2.copy(_4 = i))
      BitMapAlbum(bitMapAlbum.nome, (bitMapAlbum.data + (i -> new2)) + (j -> new1))
    }
    else {
      println("Out of bounds!")
      bitMapAlbum
    }
  }

  private def printQTreeAlbum(bitMapAlbum: BitMapAlbum): Unit = {
    println("Nome: " + bitMapAlbum.nome)
    bitMapAlbum.data.toList foreach (x => println("Key: " + x._1 + " | Directory: " + x._2._2._1 + " | Name: " + x._2._2._2 + " | Size: " + x._2._2._3 + " | Index: " + x._2._2._4))
  }

  def showContent()(bitMapAlbum: BitMapAlbum): BitMapAlbum = {
    if (bitMapAlbum.data.isEmpty)
      println("Empty Album!")
    else
      printQTreeAlbum(bitMapAlbum)
    BitMapAlbum(bitMapAlbum.nome, bitMapAlbum.data)
  }
}
