package twizansk.hivemind.test;

import akka.actor.UntypedActor;

public interface Initializer {
	void init(UntypedActor actor) throws Exception;
}