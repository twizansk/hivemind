package twizansk.hivemind.test;

import akka.actor.Props;
import akka.actor.UntypedActor;

public class MockActor extends UntypedActor {

	private Object lastMessage;
	
	public static Props makeProps() {
		return Props.create(MockActor.class);
	}
	
	@Override
	public void onReceive(Object msg) throws Exception {
		this.lastMessage = msg;
	}
	
	public Object getLastMessage() {
		return lastMessage;
	}
	
}