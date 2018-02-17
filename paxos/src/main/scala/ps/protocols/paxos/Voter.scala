package ps.protocols.paxos

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Actor, ActorRef, ActorSelection}
import ps.protocols.paxos.PaxosProtocol._

class Voter(val peerId: Int, val peerStateSynch: AtomicReference[PeerState]) extends Actor {

  val arbiter: ActorSelection = context.actorSelection("akka://Paxos/user/arbiter")

  def receive = {
    /*
   The peer must grant permission for requests with Suggestion IDs equal to or higher than any
   they have previously granted permission for. In doing so, the peer implicitly promises to
   reject all Permission Request and Suggestion messages with lower Suggestion IDs. Consequently,
   requests with IDs less than the ID last granted permission to must be ignored or responded
   to with a Nack message.
    */

    case PermissionRequest(suggestionId) =>
      println(s"PR: Got $suggestionId")
      val peerState = peerStateSynch.get()
      val lastPromisedSuggestionId = peerState.lastPromisedSuggestionId
      if (suggestionId.compareTo(lastPromisedSuggestionId) >= 0) {
        peerStateSynch.set(peerState.withLastPromisedSuggestionId(suggestionId))
        sender ! PermissionGranted(suggestionId, peerState.lastAcceptedSuggestionId, peerState.value)
      } else {
        sender ! NegativeAcknowledgment(suggestionId)
      }

    /*
    The peer must accept the Suggestion if its Suggestion ID is equal to or higher than any it has
    previously granted permission for. Otherwise, ignore the message or respond with a Nack.
     */
    case Suggestion(suggestionId, suggestedValue) =>
      println(s"SG: Got $suggestionId with $suggestedValue")
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
