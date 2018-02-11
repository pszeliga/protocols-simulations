package ps.protocols.paxos

object PaxosProtocol {

  case class PermissionRequest(suggestionId: SuggestionId)

  case class PermissionGranted(suggestionId: SuggestionId, lastSuggestionId: SuggestionId, lastAcceptedValue: Integer)

  case class Suggestion(suggestionId: SuggestionId, value: Integer)

  case class Accepted(suggestionId: SuggestionId)

  case class NegativeAcknowledgment(suggestionId: SuggestionId)

  case class PeerRequest(value: Integer)

  case class SuggestionId(timestamp: Long, peerId: Int) extends Comparable[SuggestionId] {

    def this(peerId: Int) = this(0L, peerId)

    override def compareTo(other: SuggestionId): Int = {
      val timeCompare = timestamp.compareTo(other.timestamp)
      if (timeCompare == 0) peerId.compareTo(other.peerId) else timeCompare
    }
  }



}
