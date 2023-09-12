package TextBasedInterface

import scala.annotation.tailrec
import scala.collection.SortedMap
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}


object IO_Utils {

  case class CommandLineOption(name: String, exec: QTreeTBI => QTreeTBI)

  def getUserInputInt(msg: String): Try[Int] = {
    print(msg + "\nOption: ")
    Try(readLine.trim.toUpperCase.toInt)
  }

  def prompt(msg: String): String = {
    print(msg + ": ")
    scala.io.StdIn.readLine()
  }

  @tailrec
  def optionPrompt(options: SortedMap[Int, CommandLineOption]): Option[CommandLineOption] = {
    println("\n     Options\n_________________")
    options.toList foreach
      ((option: (Int, CommandLineOption)) => println(option._1 + ") " + option._2.name))

    getUserInputInt("\nSelect an option") match {
      case Success(i) => options.get(i)
      case Failure(_) => println("Invalid number!"); optionPrompt(options)
    }
  }
}
