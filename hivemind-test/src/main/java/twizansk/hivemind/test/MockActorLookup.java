package twizansk.hivemind.test;

import twizansk.hivemind.common.ActorLookup;
import akka.actor.ActorIdentity;

class MockActorLookup extends ActorLookup {
	
	private final MockCallback callback;
	private final String path;
	
	public MockActorLookup(MockCallback callback, String path) {
		super(path, null, null);
		this.callback = callback;
		this.path = path;
	}

	@Override
	public void sendLookup() {
		callback.onExecute();
	}
	
	@Override
	public boolean isTarget(ActorIdentity identity) {
		return identity.getRef().path().toString().equals(this.path);
	}
	
}