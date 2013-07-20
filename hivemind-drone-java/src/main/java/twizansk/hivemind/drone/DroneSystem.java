package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.DataConfig;
import akka.actor.ActorSystem;

/**
 * Initializes the actor system for a drone node.
 * 
 * @author Tommer Wizansky
 *
 */
public class DroneSystem {
	
	public void init(DroneConfig config, DataConfig dataConfig) {
		final ActorSystem system = ActorSystem.create("DroneSystem");
		
		// Create the drone actor.
		system.actorOf(Drone.makeProps(
				config.objectiveFunction, 
				dataConfig.createTrainingSet(), 
				system), 
			"drone");
	}
	
	public static void main(String[] args) {
		
	}
	
}
