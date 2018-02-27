package ps.protocols.paxos

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import ps.protocols.paxos.PaxosProtocol.{PeerRequest, Suggestion, SuggestionId}
import ps.protocols.paxos.roles.{Arbiter, Suggester, Voter}
import akka.pattern.ask

import scala.concurrent.duration._
import akka.util.Timeout

import scala.concurrent.Await
import scala.util.Random

class Network(val peers: IndexedSeq[(Int, (ActorRef, ActorRef))]) extends Actor {

  private implicit val timeout = Timeout(5 seconds)
  private val random = new Random()

  def receive = {
    case newValue: Int => {
      val randomPeer = random.nextInt(Parameters.numberOfPeers - 1)
      val response = peers(randomPeer)._2._2 ? PeerRequest(newValue)
      Await.result(response, 5 second)
    }
    case any: Any => println(s"Cluster got got: $any")
  }
}

object Main extends App {

  private val random = new Random()

  val system = ActorSystem("Paxos")
  val peers: IndexedSeq[(Int, (ActorRef, ActorRef))] = (1 to Parameters.numberOfPeers)
    .map(id => {
      val initialSuggestionId = new SuggestionId(id)
      val initialState = new AtomicReference[PeerState](new PeerState(id, 0, initialSuggestionId, initialSuggestionId))
      (id, (
        system.actorOf(Props(classOf[Voter], id, initialState), s"voter-${id.toString}"),
        system.actorOf(Props(classOf[Suggester], id, initialState), s"suggester-${id.toString}")))
    })
  val cluster = system.actorOf(Props(new Network(peers)))
  val arbiter = system.actorOf(Props(classOf[Arbiter]), "arbiter")

  for (_ <- 1 to 100) {
    val randomValue = random.nextInt()
    cluster ! randomValue
  }

  Thread.sleep(10000)
  system.terminate()

}