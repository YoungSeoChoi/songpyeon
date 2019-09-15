package pso

import akka.actor.ActorSystem
import impure.impure
import pso.SwarmAdmin.initialize

import scala.io.StdIn

object SwarmMain {
  @impure
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("swarm-system")

    try {
      val admin = system.actorOf(SwarmAdmin.props(), "swarm-admin")
      admin ! initialize(Util.swarmSize)
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }
}
