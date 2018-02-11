# protocols-simulations
Java simulations of a few interesting protocols 


**Paxos**

_Paxos is a mechanism for achieving consensus on a single value over unreliable communication channels._

- any peer can make a suggestion or override values of the others
- eventually, majority will decide on final value
- all peers, share the responsibility for ensuring that consensus is eventually reached


Source: https://understandingpaxos.wordpress.com/

Implementation specific details:

1. How failed nodes are detected?

2. How are the collisions between multiple peers making simultaneous suggestions handled?

3. What ensures that all peers learnt the final result after consensus had been reached?

4. What is the retry mechanism which ensures that a result is eventually achieved?  

5. Are there any latency optimizations?

