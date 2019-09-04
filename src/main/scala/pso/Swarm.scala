package pso

import akka.actor.{Actor, ActorLogging, Props}
import impure.impure
import pso.SwarmAdmin.{swarmUpdate, swarmUpdateLocal}

import scala.util.Random

object Swarm {
  def apply(posx: Double, posy: Double): Swarm = {
    val s = new Swarm
    s.curPos = (posx, posy)
    s.curVel = (Util.width * Random.nextDouble() * 0.1, Util.height * Random.nextDouble() * 0.1)
    val fit = Util.eval(s.curPos._1, s.curPos._2)
    s.localMax = (s.curPos._1, s.curPos._2, fit)
    s.globalMax = s.localMax
    s
  }

  def props(x: Double, y: Double): Props = Props(Swarm(x, y))

  final case class swarmInit()
  final case class normalUpdate()
  final case class globalUpdate(g: (Double, Double, Double))
}

class Swarm extends Actor with ActorLogging {
  import Swarm._

  // max position x, max position y, max value
  private var localMax: (Double, Double, Double) = _
  private var globalMax: (Double, Double, Double) = _

  private var curPos: (Double, Double) = _
  private var curVel: (Double, Double) = _

  override def preStart(): Unit = log.info("\nSwarm start" )
  override def postStop(): Unit = log.info("\nSwarm stop")

  @impure
  override def receive: Receive = {
    case swarmInit() =>
      sender() ! swarmUpdateLocal(curPos, curVel, localMax)

    case normalUpdate() =>
      if (Util.checkEnd(curPos)) context.stop(self)
      log.info(s"\npos: $curPos, vel: $curVel")
      // position and velocity update
      val nextVel = Util.nextVelocity(curVel, curPos, (localMax._1, localMax._2), (globalMax._1, globalMax._2))
      curVel = nextVel
      curPos = (curPos._1 + nextVel._1, curPos._2 + nextVel._2)

      // local update
      val curFit = Util.eval(curPos._1, curPos._2)
      if (curFit > localMax._3) {
        localMax = (curPos._1, curPos._2, curFit)
        if (localMax._3 > globalMax._3) {
          globalMax = localMax
          sender() ! swarmUpdateLocal(curPos, curVel, localMax)
        } else {
          sender() ! swarmUpdate(curPos, curVel)
        }
      } else {
        sender() ! swarmUpdate(curPos, curVel)
      }

    case globalUpdate(g) =>
      if (Util.checkEnd(curPos)) context.stop(self)
      log.info(s"pos: $curPos, vel: $curVel")
      // global update
      if (g._3 > globalMax._3) globalMax = g

      // position and velocity update
      val nextVel = Util.nextVelocity(curVel, curPos, (localMax._1, localMax._2), (globalMax._1, globalMax._2))
      curVel = nextVel
      curPos = (curPos._1 + nextVel._1, curPos._2 + nextVel._2)

      // local update
      val curFit = Util.eval(curPos._1, curPos._2)
      if (curFit > localMax._3) {
        localMax = (curPos._1, curPos._2, curFit)
        if (localMax._3 > globalMax._3) {
          globalMax = localMax
          sender() ! swarmUpdateLocal(curPos, curVel, localMax)
        } else {
          sender() ! swarmUpdate(curPos, curVel)
        }
      } else {
        sender() ! swarmUpdate(curPos, curVel)
      }
  }
}

