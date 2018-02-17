package ps.protocols.paxos

import akka.actor.Actor
import ps.protocols.paxos.PaxosProtocol.{Accepted, PermissionRequest, SuggestionId}

import scala.collection.mutable

class Arbiter extends Actor {

  private val counters = new mutable.HashMap[SuggestionId, Integer]()
  private var counter = 0

  def receive = {
    case Accepted(newSuggestion, newValue) =>
      counters.get(newSuggestion) match {
        case Some(count) if count + 1 < Parameters.majorityReached => counters.update(newSuggestion, count + 1)
        case Some(count) if count + 1 >= Parameters.majorityReached =>
          println(s"Consensus reached for $newSuggestion and value $newValue")
          counter = counter + 1
          println(s"Counter: $counter")
          counters.remove(newSuggestion)
        case None => counters.put(newSuggestion, 1)
      }


  }

}
