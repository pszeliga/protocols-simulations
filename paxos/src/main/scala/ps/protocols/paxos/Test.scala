package ps.protocols.paxos

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import ps.protocols.paxos.PaxosProtocol.{PeerRequest, Suggestion, SuggestionId}

class Network(val peers: IndexedSeq[(Int, ActorRef)]) extends Actor {

  def receive = {

    case m: String => peers(0)._2 ! PeerRequest(4)//peers.foreach(peer => peer._2 ! Suggestion(new SuggestionId(1234L, 'a'), 35))


    case any: Any => println(s"Network got: $any")
  }
}

object Main extends App {
  val system = ActorSystem("Paxos")
  val peers: IndexedSeq[(Int, ActorRef)] = (1 to Parameters.numberOfPeers).map(id => (id, system.actorOf(Props(classOf[Peer], id), id.toString)))
  val network = system.actorOf(Props(new Network(peers)))

  network ! "hello"
//  network ! "buenos dias"
}