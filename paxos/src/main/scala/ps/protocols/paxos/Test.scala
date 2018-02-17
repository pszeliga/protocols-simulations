package ps.protocols.paxos

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import ps.protocols.paxos.PaxosProtocol.{PeerRequest, Suggestion, SuggestionId}

import scala.util.Random

class Network(val peers: IndexedSeq[(Int, (ActorRef, ActorRef))]) extends Actor {

  val random = new Random()

  def receive = {

    case m: String => {
      val randomPeer = random.nextInt(Parameters.numberOfPeers - 1)
      val randomValue = random.nextInt()
      peers(randomPeer)._2._2 ! PeerRequest(randomValue)
    }//peers.foreach(peer => peer._2 ! Suggestion(new SuggestionId(1234L, 'a'), 35))


    case any: Any => println(s"Network got: $any")
  }
}

object Main extends App {
  val system = ActorSystem("Paxos")
  val peers: IndexedSeq[(Int, (ActorRef, ActorRef))] = (1 to Parameters.numberOfPeers)
    .map(id => {
      val initialSuggestionId = new SuggestionId(id)
      val initialState = new AtomicReference[PeerState](new PeerState(id, 0, initialSuggestionId, initialSuggestionId))
      (id, (
        system.actorOf(Props(classOf[Voter], id, initialState), s"voter-${id.toString}"),
        system.actorOf(Props(classOf[Suggester], id, initialState), s"suggester-${id.toString}")))
    })
  val network = system.actorOf(Props(new Network(peers)))
  val arbiter = system.actorOf(Props(classOf[Arbiter]), "arbiter")

  for (_ <- 1 to 100) {

    network ! "new request"

  }

  Thread.sleep(10000)
  system.terminate()

}