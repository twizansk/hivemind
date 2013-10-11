package twizansk.hivemind.common;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorContext;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Identify;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * {@link RemoteActor} is responsible for encapsulating a remote actor address
 * and posting identification requests on behalf of a client actor
 * 
 * @author Tommer Wizansky
 * 
 */
public class RemoteActor {
	
	private final static int RETRY_PERIOD = 1;
	private final static Object LOOKUP = new Object();
	private final static Object START = new Object();
	
	public static class LookupActor extends UntypedActor {

		private final String path; 
		private final ActorRef owner;
		private volatile boolean found = false;
		
		public LookupActor(String path, ActorRef owner) {
			this.path = path;
			this.owner = owner;
		}
		
		@Override
		public void onReceive(Object msg) throws Exception {	
			if (msg instanceof ActorIdentity && isTarget((ActorIdentity) msg)) {
				this.owner.tell(msg, getSender());
				this.found = true;
			} if (msg.equals(START)) {
				this.found = false;
				getSelf().tell(LOOKUP, getSelf());
			} else if (!found && msg.equals(LOOKUP)) {
				ActorSelection selection =  this.getContext().actorSelection(path);
				selection.tell(new Identify(path), this.getSelf());
				getContext().system().scheduler().scheduleOnce(
						Duration.create(RETRY_PERIOD, TimeUnit.SECONDS),
						getSelf(), 
						LOOKUP, 
						getContext().dispatcher(), 
						getSelf());
			}
		}
		
		/**
		 * Check whether a given identity represents the lookup's target actor.
		 * 
		 * @param identity
		 * @return
		 * 		True if target. False otherwise.
		 */
		public boolean isTarget(ActorIdentity identity) {
			return identity.getRef() != null && identity.getRef().path().toString().equals(this.path);
		}
		
	}
	
	private final ActorRef lookupActor;
	private ActorRef ref;
	
	public RemoteActor(ActorRef owner, String path, ActorContext context) {
		this.lookupActor = context.system().actorOf(Props.create(LookupActor.class, path, owner));
	}
	
	/**
	 * Send an asynchronous identification request to the target.  Route the response back to the client actor.
	 */
	public void lookup() {
		this.lookupActor.tell(START, null);
	}
	
	/**
	 * Notify the remote actor object that the connection has been terminated.  The decision as to if and when to 
	 * reconnect is left to the owner.
	 */
	public void terminated() {
		this.ref = null;
	}
	
	/**
	 * Get the underlying {@link ActorRef}
	 */
	public ActorRef ref() {
		return ref;
	}
	
	/**
	 * @return
	 * 		True if the remote actor is connected and false otherwise.
	 */
	public boolean isConnected() {
		return ref != null;
	}
	
	/**
	 * Set the underlying {@link ActorRef}
	 * @param ref
	 * 		The {@link ActorRef}
	 */
	public void setRef(ActorRef ref) {
		this.ref = ref;
	}
	
	
}
