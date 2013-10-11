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
		ActorRef drone = system.actorOf(Drone.makeProps(config), "drone");
		drone.tell(MsgConnectAndStart.instance(), null);
	}
	
	public static void main(String[] args) throws Exception {
		String confFile = System.getProperty("hivemind.conf");
		if (confFile == null) {
			confFile = "hivemind"; 
		}
		Config config = ConfigFactory.load(confFile);
		DroneConfig droneConfig = DroneConfig.createConfig(config);
		new DroneSystem().init(droneConfig);
	}
	
}
