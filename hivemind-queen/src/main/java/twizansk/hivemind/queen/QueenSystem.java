package twizansk.hivemind.queen;

import twizansk.hivemind.messages.queen.Model;
import akka.actor.ActorSystem;

/**
 * Initializes the actor system for the queen node.
 * 
 * @author twizansk
 *
 */
public class QueenSystem {
	
	public void init() {
		final ActorSystem system = ActorSystem.create("QueenSystem");
		
		// Create the queen actor.
		system.actorOf(Queen.makeProps(new Model(), new ModelUpdater()), "queen");
	}
	
	public static void main(String[] args) {
		new QueenSystem().init();
	}
}
