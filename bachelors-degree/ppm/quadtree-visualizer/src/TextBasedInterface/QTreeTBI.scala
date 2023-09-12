package TextBasedInterface

import QTree.QTreeUtils._
import QTree._
import IO_Utils._
import QTree.Random.MyRandom
import java.io.File
import scala.annotation.tailrec
import scala.collection.immutable.SortedMap
import scala.util.{Failure, Success}

case class QTreeTBI(path: String, qt: QTree[Coords]){

  def selectNewImage(path: => String): QTreeTBI = QTreeTBI.selectNewImage(path)(this)
  def makeQTree(): QTreeTBI = QTreeTBI.makeQTree()(this)
  def scale(factor: => Double): QTreeTBI = QTreeTBI.scale(factor)(this)
  def mirrorV(): QTreeTBI = QTreeTBI.mirror()(this)
  def rotateR(): QTreeTBI = QTreeTBI.rotate()(this)
  def applyNoise(): QTreeTBI = QTreeTBI.mapColourEffect()(this)

}

object QTreeTBI {

  @tailrec
  def getValidPath(path: String): String = {
    if(path.equals("0")) sys.exit()
    val file = new File(path)
    if(file.exists() && (file.getPath.endsWith(".png") || file.getPath.endsWith(".gif") || file.getPath.endsWith(".jpg"))) path
    else {
      println("Invalid Path!")
      getValidPath(IO_Utils.prompt("\tTry again or press 0 to exit"))
    }
  }

  private def verifyQTree(tbi: QTreeTBI): Boolean = {
    tbi.qt match {
      case QEmpty =>
        println("It is not possible to apply effects to a QTree.QEmpty! Run makeQTree first or select a new valid directory.")
        false
      case _ => true
    }
  }

  private def getInput(msg: String): Int = {
    val input = IO_Utils.getUserInputInt(msg)
    input match {
      case Success(value) => value
      case Failure(_) => 0
    }
  }

  def selectNewImage(path: => String)(tbi: QTreeTBI): QTreeTBI = {
    val dir = getValidPath(path)
    QTreeTBI(dir, QEmpty)
  }

  def makeQTree()(tbi: QTreeTBI): QTreeTBI = {
    val qt = QuadTree.makeQTree(BitMap.createBitMapInt(tbi.path))
    writeQuadTree(qt, "TempFiles/temp.png", "png")
    QTreeTBI(tbi.path, qt)
  }

  def scale(factor: => Double)(tbi: QTreeTBI): QTreeTBI = {
    if (!verifyQTree(tbi)) return tbi
    val qt = QuadTree.scale(factor, tbi.qt)
    writeQuadTree(qt, "TempFiles/temp.png", "png")
    QTreeTBI(tbi.path, qt)
  }

  def rotate()(tbi: QTreeTBI): QTreeTBI = {
    if (!verifyQTree(tbi)) return tbi
    def aux(effect: Int): QTreeTBI = {
      effect match {
        case 1 =>
          val qt = QuadTree.rotateL(tbi.qt)
          writeQuadTree(qt, "TempFiles/temp.png", "png")
          QTreeTBI(tbi.path, qt)
        case 2 =>
          val qt = QuadTree.rotateR(tbi.qt)
          writeQuadTree(qt, "TempFiles/temp.png", "png")
          QTreeTBI(tbi.path, qt)
        case _ => println("Invalid Input!"); tbi
      }
    }
    aux(getInput("\t1) RotateL\n\t2) RotateR"))
  }

  def mirror()(tbi: QTreeTBI): QTreeTBI = {
    if (!verifyQTree(tbi)) return tbi
    def aux(effect: Int): QTreeTBI = {
      effect match {
        case 1 =>
          val qt = QuadTree.mirrorH(tbi.qt)
          writeQuadTree(qt, "TempFiles/temp.png", "png")
          QTreeTBI(tbi.path, qt)
        case 2 =>
          val qt = QuadTree.mirrorV(tbi.qt)
          writeQuadTree(qt, "TempFiles/temp.png", "png")
          QTreeTBI(tbi.path, qt)
        case _ => println("Invalid Input!"); tbi
      }
    }
    aux(getInput("\t1) MirrorH\n\t2) MirrorV"))
  }

  def mapColourEffect()(tbi: QTreeTBI): QTreeTBI = {
    if (!verifyQTree(tbi)) return tbi
    def aux(effect: Int): QTreeTBI = {
      effect match {
        case 1 =>
          val qt = QuadTree.mapColourEffect(Noise, tbi.qt)
          writeQuadTree(qt, "TempFiles/temp.png", "png")
          QTreeTBI(tbi.path, qt)
        case 2 =>
          val qt = QuadTree.mapColourEffect_1(Noise_1, MyRandom(1), tbi.qt)
          writeQuadTree(qt, "TempFiles/temp.png", "png")
          QTreeTBI(tbi.path, qt)
        case 3 =>
          val qt = QuadTree.mapColourEffect(Sepia, tbi.qt)
          writeQuadTree(qt, "TempFiles/temp.png", "png")
          QTreeTBI(tbi.path, qt)
        case 4 =>
          val qt = QuadTree.mapColourEffect(Contrast, tbi.qt)
          writeQuadTree(qt, "TempFiles/temp.png", "png")
          QTreeTBI(tbi.path, qt)
        case _ => println("Invalid Input!"); tbi
      }
    }
    aux(getInput("\t1) Noise\n\t2) Noise_1\n\t3) Sepia\n\t4) Contrast"))
  }
}

object TextBasedInterface extends App{

  val path = QTreeTBI.getValidPath(IO_Utils.prompt("Insert the image directory"))
  val content = QTreeTBI(path, QEmpty)

  val options = SortedMap[Int, CommandLineOption](
    6-> CommandLineOption("MapColourEffect\n_________________", QTreeTBI.mapColourEffect()),
    5-> CommandLineOption("Rotate", QTreeTBI.rotate()),
    4-> CommandLineOption("Mirror", QTreeTBI.mirror()),
    3-> CommandLineOption("Scale", QTreeTBI.scale(IO_Utils.prompt("\tInsert scale factor").toDouble)),
    2-> CommandLineOption("MakeQTree", QTreeTBI.makeQTree()),
    1 -> CommandLineOption("Select New Image", QTreeTBI.selectNewImage(IO_Utils.prompt("\nInsert new image directory"))),
    0-> CommandLineOption("Exit", _ => {
      val file = new File("TempFiles/temp.png")
      if(file.exists()) file.delete()
      sys.exit()
    })
  )

  @tailrec
  def mainLoop(content: QTreeTBI) {
    println(content.qt)
    IO_Utils.optionPrompt(options) match {
      case Some(opt) => val newQA = opt.exec(content); mainLoop(newQA)
      case _ => println("Invalid option"); mainLoop(content)
    }
  }

  val file = new File("TempFiles/temp.png")
  if(file.exists()) file.delete()

  mainLoop(content)

}

