package twizansk.hivemind.test;

import twizansk.hivemind.common.ActorLookup;
import twizansk.hivemind.common.ActorLookupFactory;
import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class MockActorLookupFactory implements ActorLookupFactory {

	private final MockCallback callback;
	private final String path;
	
	public MockActorLookupFactory(MockCallback callback, String path) {
		this.callback = callback;
		this.path = path;
	}

	@Override
	public ActorLookup create(ActorContext context, ActorRef ref) {
		return new MockActorLookup(callback, path);
	}
}