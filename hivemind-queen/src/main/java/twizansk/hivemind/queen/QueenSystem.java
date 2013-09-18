package twizansk.hivemind.queen;

import akka.actor.ActorSystem;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Initializes the actor system for the queen node.
 * 
 * @author twizansk
 *
 */
public class QueenSystem {
	
	public void init(QueenConfig config) {
		final ActorSystem system = ActorSystem.create("QueenSystem");
		
		// Create the queen actor.
		system.actorOf(Queen.makeProps(config), "queen");
	}
	
	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load("queen");
		QueenConfig queenConfig = QueenConfig.createConfig(config);
		new QueenSystem().init(queenConfig);
	}
}
