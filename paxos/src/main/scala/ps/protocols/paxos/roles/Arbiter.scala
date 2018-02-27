package ps.protocols.paxos.roles

import akka.actor.Actor
import ps.protocols.paxos.Parameters
import ps.protocols.paxos.PaxosProtocol.{Accepted, SuggestionId}

import scala.collection.mutable

class Arbiter extends Actor {

  private val counters = new mutable.HashMap[SuggestionId, Integer]()

  def receive = {
    case Accepted(newSuggestion, newValue) =>
      counters.get(newSuggestion) match {
        case Some(count) if count + 1 < Parameters.majorityReached => counters.update(newSuggestion, count + 1)
        case Some(count) if count + 1 >= Parameters.majorityReached =>
          println(s"Consensus reached for $newSuggestion and value $newValue")
          counters.remove(newSuggestion)
        case None => counters.put(newSuggestion, 1)
      }
  }
}
