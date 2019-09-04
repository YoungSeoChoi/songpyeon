package pso

import scala.util.Random
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import impure.impure
import pso.Swarm.{globalUpdate, normalUpdate, swarmInit}

object SwarmAdmin {
  def props(): Props = Props(new SwarmAdmin(Vector(), Array()))

  final case class initialize(n: Int)
  final case class globalSetUp(global: (Double, Double, Double))
  final case class swarmUpdate(curPos: (Double, Double), curVel: (Double, Double))
  final case class swarmUpdateLocal(curPos: (Double, Double), curVel: (Double, Double), local: (Double, Double, Double))
}

class SwarmAdmin(private var group: Vector[ActorRef], private var radar: Array[((Double, Double), (Double, Double))]) extends Actor with ActorLogging {
  import SwarmAdmin._

  override def preStart(): Unit = log.info("\nAdmin start")

  private var globalMax: (Double, Double, Double) = null

  // No need to handle any messages
  @impure
  override def receive: Receive = {
    case initialize(n) =>
      radar = Array.fill(n)((0,0), (0,0))
      for (i <- 0 until n) {
        group = context.actorOf(Swarm.props(Random.nextInt(1800), Random.nextInt(1000)), (n - i - 1).toString) +: group
      }
      group.foreach(_ ! swarmInit())

    case swarmUpdate(pos, vel) =>
      val name = sender().path.name
      radar(name.toInt) = (pos, vel)
      sender() ! normalUpdate()

    case swarmUpdateLocal(pos, vel, local) =>
      val name = sender().path.name
      radar(name.toInt) = (pos, vel)
      if (globalMax == null || local._3 > globalMax._3) {
        globalMax = local
        group.foreach(_ ! globalUpdate(globalMax))
      } else {
        sender() ! normalUpdate()
      }

  }
}
