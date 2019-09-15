package cui

import pso.Util

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Painter {

  def main(args: Array[String]): Unit ={
    for (i <- 1 to 10) {
      Thread.sleep(500)
      println("\n" * 5)
      println(drawSongpyeon(drawEdge(130, 30)))
    }
  }

  def drawEdge(width: Int, height: Int): String = {
    assert(width >= 2 && height >= 2)
    val horizontal = "─"
    val vertical = "|"
    val top = "*" + horizontal * (width - 2) + "*" + "\n"
    val middle = (vertical + " " * (width - 2) + vertical + "\n") * (height - 2)
    top + middle + top
  }

  def drawSongpyeon(before: String): String = {
    val sortedSong = Await.result(Util.songLoc, Duration.Inf).sortBy(-_._3)
    val beforeMat = before.split("\n")

    val x = sortedSong.head
    val tmp = " " * (x._1.toInt - 1) + "S" + " " * (Util.width - x._1).toInt
    val vec = for {
      i <- beforeMat(x._2.toInt) zip tmp
    } yield if (i._2 == ' ') i._1 else i._2
    beforeMat(x._2.toInt) = vec.mkString("")

    val ret = (beforeMat /: sortedSong.tail)((l, s) => {
      val tmp = " " * (s._1.toInt - 1) + "+" + " " * (Util.width - s._1).toInt
      val vec = for {
        i <- l(s._2.toInt) zip tmp
      } yield if (i._2 == ' ') i._1 else i._2
      l(s._2.toInt) = vec.mkString("")
      l
    })
    ret.mkString("\n")
  }

}
