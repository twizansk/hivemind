package twizansk.hivemind.common;

import akka.actor.ActorContext;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Identify;

/**
 * {@link ActorLookup} is responsible for encapsulating a remote actor address
 * and posting identification requests on behalf of a client actor
 * 
 * @author Tommer Wizansky
 * 
 */
public class ActorLookup {
	
	private final String path;
	private final ActorContext context;
	private final ActorRef clientActor;
	
	public ActorLookup(String address, ActorContext context, ActorRef clientActor) {
		super();
		this.path = address;
		this.context = context;
		this.clientActor = clientActor;
	}
	
	/**
	 * Send an asynchronous identification request to the target.  Route the response back to the client actor.
	 */
	public void sendLookup() {
		ActorSelection queenSelection =  this.context.system().actorSelection(path);
		queenSelection.tell(new Identify(path), clientActor);
//		System.out.println("send lookup to: " + queenSelection.toString());
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
