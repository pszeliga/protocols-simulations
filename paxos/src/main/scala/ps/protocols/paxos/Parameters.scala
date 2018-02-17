package ps.protocols.paxos

object Parameters {

  val numberOfPeers: Int = 7

  val majorityReached: Int = (numberOfPeers / 2) + 1

}
