package twizansk.hivemind.drone;

import twizansk.hivemind.messages.external.MsgConnectAndStart;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Initializes the actor system for a drone node.
 * 
 * @author Tommer Wizansky
 *
 */
public class DroneSystem {
		
	public void init(DroneConfig config) throws Exception {
		final ActorSystem system = ActorSystem.create("DroneSystem");
		
		// Create the drone actor and start it.
		ActorRef drone = system.actorOf(Drone.makeProps(config), 
			"drone");
		drone.tell(MsgConnectAndStart.instance(), null);
	}
	
	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load("drone");
		Config dataConfig = ConfigFactory.load("data");
		DroneConfig droneConfig = DroneConfig.createConfig(config, dataConfig);
		new DroneSystem().init(droneConfig);
	}
	
}
