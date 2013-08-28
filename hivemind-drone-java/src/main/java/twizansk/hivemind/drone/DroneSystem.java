package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.TrainingSet;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.common.DefaultActorLookupFactory;
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
		
	public void init(IObjectiveFunction objective, TrainingSet trainingSet, String queenPath) throws Exception {
		final ActorSystem system = ActorSystem.create("DroneSystem");
		
		// Create the drone actor and start it.
		ActorRef drone = system.actorOf(Drone.makeProps(
				objective, 
				trainingSet, 
				new DefaultActorLookupFactory(queenPath)), 
			"drone");
		drone.tell(MsgConnectAndStart.instance(), null);
	}
	
	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load("drone");
		Config dataConfig = ConfigFactory.load("data");
		
		String objectiveClassName = config.getString("hivemind.drone.objective");
		String trainingSetClassName = dataConfig.getString("hivemind.data.trainingSet");
		String queenPath = config.getString("hivemind.queen.path");
		
		Class<?> objectiveClass = Class.forName(objectiveClassName);
		Class<?> trainingSetClass = Class.forName(trainingSetClassName);
		
		IObjectiveFunction objective = (IObjectiveFunction) objectiveClass.newInstance();
		TrainingSet trainingSet = (TrainingSet) trainingSetClass.newInstance();
		
		new DroneSystem().init(objective, trainingSet, queenPath);
	}
	
}
