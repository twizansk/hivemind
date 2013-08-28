package twizansk.hivemind.common;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

/**
 * The default implemlentation of {@link ActorLookupFactory}.  Returns a simple {@link ActorLookup} instance.
 * 
 * @author Tommer Wizansky
 *
 */
public class DefaultActorLookupFactory implements ActorLookupFactory {

	private final String path;
	
	public DefaultActorLookupFactory(String path) {
		this.path = path;
	}

	@Override
	public ActorLookup create(ActorContext context, ActorRef ref) {
		return new ActorLookup(path, context, ref);
	}

}
