package ps.protocols.twopc.roles

import akka.actor.Actor
import ps.protocols.twopc.TwoPhaseCommitProtocol._
import java.math.BigDecimal
import java.util.UUID

import ps.protocols.utils.Simulation

import scala.collection.mutable
import scala.util.Random

class Resource(val peerId: Int) extends Actor {

  val queryToAmount = new mutable.HashMap[UUID, BigDecimal]
  var currentAmount = BigDecimal.ZERO

  def receive = {
    case Query(uuid, change) =>
      Simulation.simulateTimeoutOnceInAWhile(MAX_WAIT_TIME)
      println(s"Resource $peerId received amount change query: $uuid change: $change")
      val isOk = Simulation.ninetyPercentChance() // just one is too rare
      if(isOk) {
        queryToAmount.put(uuid, change)
      }
      sender ! Vote(isOk)

    case Commit(uuid) =>
      println(s"Resource $peerId commiting the query $uuid")
      queryToAmount.get(uuid)
        .foreach(change => currentAmount = currentAmount.add(change))
      println(s"Current amount is now: $currentAmount")
      sender ! Acknowledgement(uuid)

    case Rollback(uuid) =>
      println(s"Resource $peerId rolling back the query $uuid")
      queryToAmount.remove(uuid)
      sender ! Acknowledgement(uuid)
  }
}

object Resource {
  val rand = new Random()
}
