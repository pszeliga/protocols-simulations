package ps.protocols.paxos.roles

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

import akka.actor.{Actor, ActorSelection}
import akka.pattern.ask
import akka.util.Timeout
import ps.protocols.paxos.PaxosProtocol._
import ps.protocols.paxos.{Parameters, PeerState}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, TimeoutException}

class Suggester(val peerId: Int, val peerStateSych: AtomicReference[PeerState]) extends Actor {

  private implicit val ec = ExecutionContext.fromExecutorService(
    Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))
  private implicit val timeout = Timeout(5 seconds)

  val voters: scala.collection.immutable.IndexedSeq[ActorSelection] = getVoters(peerId)


  def getVoters(peerId: Int): scala.collection.immutable.IndexedSeq[ActorSelection] = {
    (1 to Parameters.numberOfPeers)
      .map(id => context.actorSelection("akka://Paxos/user/voter-" + id.toString))
  }

  def receive: Receive = {
    case PeerRequest(newValue) => {
      Stream.continually(suggestNewValue(newValue))
        .filter(res => res.isDefined)
        .map(op => op.get)
        .map(sid => voters.map(peer => peer ! Suggestion(sid, newValue)))
        .take(1)
      sender ! newValue
    }

  }

  private def suggestNewValue(newValue: Int): Option[SuggestionId] = {
    val newSuggestion = SuggestionId(System.nanoTime(), peerId)
    var permissionCounter = 1
    val futures = voters.map(peer => peer ? PermissionRequest(newSuggestion))
    for (future <- futures) {
      if (!(permissionCounter >= Parameters.majorityReached)) {
        try {
          val response = Await.result(future, 40 millis)
          println(s"Peer $peerId Got response for Permission request $response")
          response match {
            case PermissionGranted(_, lastSuggestion, lastValue) =>
              val peerState = peerStateSych.get()
              if (lastSuggestion.compareTo(peerState.lastAcceptedSuggestionId) > 0) {
                peerStateSych.set(new PeerState(peerState.peerId, lastValue, lastSuggestion, lastSuggestion))
                println(s"Peer $peerId dropping an attempt for $newSuggestion and value $newValue, newer suggestion was $lastSuggestion")
                return None
              }
              permissionCounter += 1
            case NegativeAcknowledgment(SuggestionId(_, _)) => permissionCounter -= 1
          }
        } catch {
          case _: TimeoutException => //timeout - counter stays the same
        }
      }
    }
    if (permissionCounter >= Parameters.majorityReached) Some(newSuggestion) else None
  }

}
