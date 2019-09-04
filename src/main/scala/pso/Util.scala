package pso

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util._

object Util {

  val width: Double = 1800.0
  val height: Double = 1000.0

  lazy val songLoc: Future[List[(Double, Double, Double)]] = Future {
    val songList: List[String] = Source.fromFile("song.txt")("UTF-8").getLines().toList
    songList.map(s => {
      val arr = s.split(" ")
      (arr(0).toDouble, arr(1).toDouble, arr(2).toDouble)
    })
  }

  lazy val songBest: Future[(Double, Double, Double)] = songLoc.map(x => x.maxBy(_._3))

  def eval(x: Double, y: Double): Double = {
    val sLoc = Await.result(songLoc, Duration.Inf)
    (0.0 /: sLoc)((z, s) => z + math.exp(s._3 - getD(x, y, s._1, s._2)))
  }

  def getD(p: Double, q: Double, x: Double, y: Double): Double = {
    math.sqrt(math.pow(p - x, 2) + math.pow(q - y, 2))
  }

  def nextVelocity(curVel: (Double, Double), curPos: (Double, Double), local: (Double, Double), global: (Double, Double)): (Double, Double) = {
    val w = 0.7
    val c1 = 1.5
    val c2 = 1.5
    val r1 = Random.nextDouble()
    val r2 = Random.nextDouble()

    val velx = w*curVel._1 + c1*r1*(local._1 - curPos._1) + c2*r2*(global._1 - curPos._1)
    val vely = w*curVel._2 + c1*r1*(local._2 - curPos._2) + c2*r2*(global._2 - curPos._2)

    (velx, vely)
  }

  def reflect(pos: (Double, Double)): (Double, Double) = {
    ???
  }

  def checkEnd(curPos: (Double, Double)) = {
    val best = Await.result(songBest, Duration.Inf)
    curPos._1 == best._1 && curPos._2 == best._2
  }
}
