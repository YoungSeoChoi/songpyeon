package cui

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import pso.SwarmAdmin.initialize
import pso.{SwarmAdmin, Util}

object CUIActor {

  def props(): Props = Props(new CUIActor)

  final case class init()
  final case class posUpdate(name: Int, pos: (Double, Double))
}

class CUIActor extends Actor {
  import CUIActor._

  private var endCond = 0

  private var screen = Painter.drawSongpyeon(Painter.drawEdge(Util.width.toInt, Util.height.toInt))

  val posarr: Array[(Double, Double)] = new Array[(Double, Double)](Util.swarmSize)

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val bufferSize = 10

  val ref: ActorRef = Source
    .actorRef[(Int, (Double, Double))](bufferSize, OverflowStrategy.dropHead)
//    .groupedWithin(10, Duration(250, "millisecond"))
    .toMat(Sink.foreach(paint))(Keep.left)
//    .toMat(Sink.foreach(print))(Keep.left)
    .run()

  override def receive: Receive = {
    case init() =>
      val admin = context.actorOf(SwarmAdmin.props(), "swarm-admin")
      admin ! initialize(Util.swarmSize)
    case posUpdate(name, pos) =>
//      if (Util.checkEnd(pos)) endCond += 1
//      if (endCond >= 100) sender ! stopSlowly
      ref ! (name, pos)
  }

  def paint(data: (Int, (Double, Double))): Unit = {
    posarr(data._1) = data._2

    if (!posarr.contains(null)) {
      val strarr = screen.split("\n")

      val drawing = (strarr /: posarr) ((l, s) => {
        if (Util.checkInside(s)) {
          val tmp = " " * s._1.toInt + "o" + " " * (Util.width.toInt - s._1.toInt)
          val vec = for {
            i <- l(s._2.toInt) zip tmp
          } yield if (i._2 == ' ') i._1 else i._2
          l(s._2.toInt) = vec.mkString("")
          l
        } else l
      })

      println("\n" * 5)
      println(drawing.mkString("\n"))
    }
  }
}
