package ps.protocols.utils

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Simulation {

  private val rand = new Random()

  def simulateTimeoutOnceInAWhile(timeout: FiniteDuration): Unit = {
    if (tenPercentChance()) {
      Thread.sleep(timeout.toMillis + 10)
    }
  }

  def tenPercentChance() = {
    rand.nextInt(10) % 10 == 1
  }

  def ninetyPercentChance() = {
     !tenPercentChance()
  }
}
