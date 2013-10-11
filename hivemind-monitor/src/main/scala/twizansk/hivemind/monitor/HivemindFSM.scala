package twizansk.hivemind.monitor

import twizansk.hivemind.common.RemoteActor
import akka.actor.Actor
import scala.collection.mutable
import akka.actor.Terminated
import akka.actor.ActorIdentity
import akka.actor.FSM
import akka.actor.Terminated
import akka.actor.ActorIdentity

/**
 * HivemindFSM augments the standard Akka FSM with methods for handling remote actors:  watching, acting on termination, reconnecting, etc.
 */
trait HivemindFSM[A, B] extends FSM[A, B]{
  
	val remoteActors = mutable.Map[String,RemoteActor]()
  
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
	
	whenUnhandled {
	  case Event(t: Terminated, _) => {
	    println("terminated")
	    remoteActors.get(t.actor.path.toString()).foreach(actor => {
		  actor.terminated()
		  actor.lookup()})
	    stay 
	  }
	  case Event(a: ActorIdentity, _) => {
	    println("connected")
	    remoteActors.get(a.getRef.path.toString()).foreach(actor => {
			actor.setRef(a.getRef)
			context.watch(a.getRef)})
	    stay
	  }
	}
}