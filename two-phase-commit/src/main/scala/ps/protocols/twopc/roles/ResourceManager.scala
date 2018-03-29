package ps.protocols.twopc.roles

import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.Executors

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import ps.protocols.twopc.TwoPhaseCommitProtocol._

import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}

class ResourceManager(val resources: IndexedSeq[ActorRef]) extends Actor {

  private implicit val ec = ExecutionContext.fromExecutorService(
    Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))
  private implicit val timeout = Timeout(MAX_WAIT_TIME)

  def receive = {
    case PeerRequest(query) =>
      sender ! PeerResponse(executeQuery(query))
  }

  private def executeQuery(change: BigDecimal): Boolean = {
    val id = UUID.randomUUID()
    val queryFutureResults = resources.map(resource => {
      resource ? Query(id, change)
    })
    val res = Future.sequence(queryFutureResults).transform(
      responses => responses.forall {
        case Vote(voteResult) => voteResult
        case _ => false
      },
      ex => ex
    ).recover {
      case ex: TimeoutException => {
        println(s"Timeout received for id $id")
        return false
      }
    }
    val result = Await.result(res, MAX_WAIT_TIME)
    println(s"Was a query $id promised by all? - $result")
    if (result) {
      askResources(Commit(id))
    } else {
      askResources(Rollback(id))
      false
    }
  }

  private def askResources(msg: Any): Boolean = {
    val result = Future.sequence(resources.map(resource => {
      resource ? msg
    })).transform(
      responses => responses.forall {
        case Acknowledgement(_) => true
        case _ => false
      },
      ex => ex
    ).recover {
      case ex: TimeoutException => {
        println(s"Timeout received for id $msg")
        return false
      }
    }
    Await.result(result, MAX_WAIT_TIME)
  }
}
