package ps.protocols.paxos.roles

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Actor, ActorSelection}
import ps.protocols.paxos.PaxosProtocol._
import ps.protocols.paxos.PeerState

class Voter(val peerId: Int, val peerStateSynch: AtomicReference[PeerState]) extends Actor {

  val arbiter: ActorSelection = context.actorSelection("akka://Paxos/user/arbiter")

  def receive = {
    case PermissionRequest(suggestionId) =>
      println(s"PR: Peer $peerId got $suggestionId")
      val peerState = peerStateSynch.get()
      val lastPromisedSuggestionId = peerState.lastPromisedSuggestionId
      if (suggestionId.compareTo(lastPromisedSuggestionId) >= 0) {
        peerStateSynch.set(peerState.withLastPromisedSuggestionId(suggestionId))
        sender ! PermissionGranted(suggestionId, peerState.lastAcceptedSuggestionId, peerState.value)
      } else {
        sender ! NegativeAcknowledgment(suggestionId)
      }

    case Suggestion(suggestionId, suggestedValue) =>
      println(s"SG: Peer $peerId got $suggestionId with $suggestedValue")
      val peerState = peerStateSynch.get()
      if (suggestionId.compareTo(peerState.lastPromisedSuggestionId) >= 0) {
        val newPeerState = new PeerState(peerState.peerId, suggestedValue, suggestionId, suggestionId)
        peerStateSynch.set(newPeerState)
        println(s"Peer $peerId changed value to ${newPeerState.value}")
        arbiter ! Accepted(suggestionId, suggestedValue)
      } else {
        arbiter ! NegativeAcknowledgment(suggestionId)
      }
  }
}
