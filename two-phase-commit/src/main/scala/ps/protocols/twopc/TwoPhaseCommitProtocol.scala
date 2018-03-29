package ps.protocols.twopc

import java.math.BigDecimal
import java.util.UUID
import scala.concurrent.duration._

object TwoPhaseCommitProtocol {

  case class Query(queryId: UUID, change: BigDecimal)

  case class Vote(isOk: Boolean)

  case class Commit(queryId: UUID)

  case class Rollback(queryId: UUID)

  case class Acknowledgement(queryId: UUID)

  case class PeerRequest(change: BigDecimal)

  case class PeerResponse(success: Boolean)

  val MAX_WAIT_TIME = 100 millis
}
