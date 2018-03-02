package ps.protocols.twopc

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.Random

class Network(val peers: IndexedSeq[(Int, (ActorRef, ActorRef))]) extends Actor {

  private implicit val timeout = Timeout(5 seconds)
  private val random = new Random()

  def receive = {
    case any: Any => println(s"Cluster got got: $any")
  }
}

object Main extends App {

//  private val random = new Random()
//
  val system = ActorSystem("Two-Phased Commit")
//  val peers: IndexedSeq[(Int, (ActorRef, ActorRef))] = (1 to Parameters.numberOfPeers)
//    .map(id => {
//      val initialSuggestionId = new SuggestionId(id)
//      val initialState = new AtomicReference[PeerState](new PeerState(id, 0, initialSuggestionId, initialSuggestionId))
//      (id, (
//        system.actorOf(Props(classOf[Voter], id, initialState), s"voter-${id.toString}"),
//        system.actorOf(Props(classOf[Suggester], id, initialState), s"suggester-${id.toString}")))
//    })
//  val cluster = system.actorOf(Props(new Network(peers)))
//  val arbiter = system.actorOf(Props(classOf[Arbiter]), "arbiter")
//
//  for (_ <- 1 to 100) {
//    val randomValue = random.nextInt()
//    cluster ! randomValue
//  }

  Thread.sleep(10000)
  system.terminate()

}