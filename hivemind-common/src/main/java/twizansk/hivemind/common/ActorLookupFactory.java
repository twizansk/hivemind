package twizansk.hivemind.common;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

/**
 * Abstract factory class for creating {@link ActorLookup} instances. This is especially
 * useful when one wants the option to inject mock and test implementations of {@link ActorLookup}
 * 
 * @author Tommer Wizansky
 * 
 */
public interface ActorLookupFactory {

	/**
	 * Create an {@link ActorLookup} instance.
	 * 
	 * @param context
	 * 		The {@link ActorContext} of the client actor.
	 * @param ref
	 * 		A reference to the client actor.
	 * @return
	 * 		The instance.
	 */
	ActorLookup create(ActorContext context, ActorRef ref);
	
}
