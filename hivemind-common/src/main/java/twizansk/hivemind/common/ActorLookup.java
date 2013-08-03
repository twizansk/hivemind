package twizansk.hivemind.common;

import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Identify;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * The role of the {@link ActorLookup} actor is to lookup and identify a remote
 * actor and return the corresponding {@link ActorRef} instance.
 * 
 * @author Tommer Wizansky
 * 
 */
public class ActorLookup extends UntypedActor {

	private final String path;
	public static final String GET_ACTOR_REF = "get-actor-ref";
	private volatile ActorRef ref;

	public ActorLookup(String queenPath) {
		this.path = queenPath;
		ActorSelection queenSelection = getContext().actorSelection(queenPath);
		queenSelection.tell(new Identify(queenPath), getSelf());
	}

	public static Props makeProps(String queenPath) {
		return Props.create(ActorLookup.class, queenPath);
	}
	
	@Override
	public void onReceive(Object msg) throws Exception {
		// Recieve the identity of the actor.
		if (msg instanceof ActorIdentity && ((ActorIdentity) msg).getRef().path().equals(path)) {
			this.ref = ((ActorIdentity) msg).getRef();
		} 
		
		// The reference to the actor was requested.
		else if (msg.equals(GET_ACTOR_REF)) {
			// If we haven't received it yet, requeue the request.
			if (ref == null) {
				getContext().system().scheduler().scheduleOnce(
						Duration.create(1, TimeUnit.SECONDS),
						getSelf(), 
						GET_ACTOR_REF, 
						getContext().system().dispatcher(), null);
			} 
			
			// Otherwise, send the reference to the requester.
			else {
				getSender().tell(ref, getSelf());
			}
		} else {
			unhandled(msg);
		}

	}

}
