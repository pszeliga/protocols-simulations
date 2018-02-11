package ps.protocols.paxos

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, ActorSelection}
import ps.protocols.paxos.PaxosProtocol._
import akka.pattern.ask
import akka.routing.{ActorSelectionRoutee, BroadcastRoutingLogic, Routee, Router}
import akka.util.Timeout
import ps.protocols.paxos.Parameters.numberOfPeers

import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}

class Peer(val peerId: Int) extends Actor {

  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
  implicit val timeout = Timeout(5 seconds)

  def getOtherPeers(peerId: Int): scala.collection.immutable.IndexedSeq[ActorSelection] = {
    (1 to Parameters.numberOfPeers)
      .filterNot(_.equals(peerId))
      .map(id => context.actorSelection("akka://Paxos/user/" + id.toString))
  }

  val otherPeers: scala.collection.immutable.IndexedSeq[ActorSelection] = getOtherPeers(peerId)

  var lastAcceptedSuggestionId = new SuggestionId(peerId)

  var lastPromisedSuggestionId = lastAcceptedSuggestionId

  var value: Integer = 0

  def receive = {
    case PeerRequest(newValue) =>
      val newSuggestion = SuggestionId(System.currentTimeMillis(), peerId)
      var acceptCounter = 0
      //      Future.sequence(otherPeers.map(peer => peer ? PermissionRequest(new SuggestionId(System.currentTimeMillis(), peerId))))
      //          .foreach(seq => println(seq.foreach(println)))
      val futures = otherPeers.map(peer => peer ? PermissionRequest(newSuggestion))
      for (future <- futures) {
        if (!(acceptCounter > Parameters.majorityReached)) {
          try {
            val response = Await.result(future, 40 millis)
            println(s"Got response for Permission request $response")
            response match {
              case PermissionGranted(suggestionId, lastSuggestion, lastValue) => {
                if (lastSuggestion.compareTo(lastAcceptedSuggestionId) > 0) {
                  lastAcceptedSuggestionId = lastSuggestion
                  lastPromisedSuggestionId = lastSuggestion
                  value = lastValue
                }
                acceptCounter += 1
              }
              case NegativeAcknowledgment(SuggestionId(ts, peerId)) => acceptCounter -= 1
            }
          } catch {
            case e: TimeoutException => acceptCounter -= 1
          }
        }
      }
      if(acceptCounter > Parameters.majorityReached) {
        val futures = otherPeers.map(peer => peer ? Suggestion(newSuggestion, newValue))

      }//

      println(acceptCounter)

      //      val router = Router(BroadcastRoutingLogic(), otherPeers)
      //      router.rouPermissionRequest(new SuggestionId(System.currentTimeMillis(), peerId))
      println(s"Got new value request: $newValue")

    //      val future = sender ? PermissionRequest(new SuggestionId(System.currentTimeMillis(), peerId))

    /*
    The peer must grant permission for requests with Suggestion IDs equal to or higher than any
    they have previously granted permission for. In doing so, the peer implicitly promises to
    reject all Permission Request and Suggestion messages with lower Suggestion IDs. Consequently,
    requests with IDs less than the ID last granted permission to must be ignored or responded
    to with a Nack message.
     */

    case PermissionRequest(suggestionId) =>
      println(s"PR: Got $suggestionId")
      if (suggestionId.compareTo(lastPromisedSuggestionId) >= 0) {
        lastPromisedSuggestionId = suggestionId
        sender ! PermissionGranted(suggestionId, lastAcceptedSuggestionId, value)
      } else {
        sender ! NegativeAcknowledgment(suggestionId)
      }

      /*
      The peer must accept the Suggestion if its Suggestion ID is equal to or higher than any it has
      previously granted permission for. Otherwise, ignore the message or respond with a Nack.
       */
    case Suggestion(suggestionId, suggestedValue) =>
      println(s"SG: Got $suggestionId with $suggestedValue")
      if (suggestionId.compareTo(lastPromisedSuggestionId) >= 0) {
        lastAcceptedSuggestionId = suggestionId
        lastPromisedSuggestionId = suggestionId
        value = suggestedValue
        sender ! Accepted(suggestionId)
      } else {
        sender ! NegativeAcknowledgment(suggestionId)
      }

  }
}

/*
- (retry after failure)If two or more peers attempt to follow these steps at approximately the same time, they may conflict with one another and prevent a majority from being achieved at either step. When this occurs, the peers simply increase their suggestion IDs and restart the resolution process.



 */