package ps.protocols.paxos

import ps.protocols.paxos.PaxosProtocol.SuggestionId

class PeerState(
     val peerId: Int,
     val value: Int,
     val lastAcceptedSuggestionId: SuggestionId,
     val lastPromisedSuggestionId: SuggestionId) {

  def withLastPromisedSuggestionId(suggestionId: SuggestionId) =
    new PeerState(peerId, value, lastAcceptedSuggestionId, suggestionId)
}
