package twizansk.hivemind.monitor

import scala.collection.mutable
import akka.actor.Actor
import akka.actor.ActorIdentity
import akka.actor.ActorIdentity
import akka.actor.FSM
import akka.actor.Terminated
import akka.actor.Terminated
import twizansk.hivemind.common.RemoteActor
import akka.actor.LoggingFSM

/**
 * HivemindFSM augments the standard Akka FSM with methods for handling remote actors:  watching, acting on termination, reconnecting, etc.
 */
trait HivemindFSM[A, B] extends LoggingFSM[A, B] {

  override def logDepth = 12
  val remoteActors = mutable.Map[String, RemoteActor]()

  /**
   * Register a remote actor with the state machine.  A remote actor will be monitored for lifecycle events and
   * will reconnect in event of termination.
   *
   * @param path
   * 		The remote path.
   * @return
   * 		A reference to the {@link RemoteActor} object.
   */
  def registerRemoteActor(path: String): RemoteActor = {
    val remoteActor = new RemoteActor(self, path, context)
    remoteActors(path) = remoteActor
    return remoteActor
  }
  
  override def receive: Receive = {
    val lifecycle: PartialFunction[Any, Unit] = {
      case t: Terminated => {
        remoteActors.get(t.actor.path.toString()).foreach(actor => {
          actor.terminated()
          actor.lookup()
        })
        stay
      }
      case a: ActorIdentity => {
        remoteActors.get(a.getRef.path.toString()).foreach(actor => {
          actor.setRef(a.getRef)
          context.watch(a.getRef)
        })
        stay
      }
    } 
    return lifecycle orElse super.receive
  }
  

}