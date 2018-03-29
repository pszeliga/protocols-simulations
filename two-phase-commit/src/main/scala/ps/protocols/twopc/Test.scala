package ps.protocols.twopc

import java.math.BigDecimal

import akka.actor.{ActorSystem, Props}
import ps.protocols.twopc.TwoPhaseCommitProtocol.PeerRequest
import ps.protocols.twopc.roles.{Resource, ResourceManager}

import scala.util.Random

object Main extends App {

  private val random = new Random()

  val system = ActorSystem("TwoPhasedCommit")
  val resources = (1 to 3).map(id => system.actorOf(Props(new Resource(id))))

  val resourceManager = system.actorOf(Props(new ResourceManager(resources)))

  for (_ <- 1 to 100) {
    val randomValue = random.nextInt()
    resourceManager ! PeerRequest(new BigDecimal(randomValue % 10))
  }

  Thread.sleep(10000)
  system.terminate()

}