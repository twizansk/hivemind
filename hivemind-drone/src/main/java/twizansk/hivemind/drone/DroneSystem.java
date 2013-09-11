package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.TrainingSet;
import twizansk.hivemind.api.model.ObjectiveFunction;
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
		
	public void init(ObjectiveFunction<?> objective, TrainingSet trainingSet, String queenPath) throws Exception {
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
		
		// Get the remote path to the queen.
		String queenPath = config.getString("hivemind.queen.path");
		
		// Initialize the objective function.
		String objectiveClassName = config.getString("hivemind.drone.objective");
		Class<?> objectiveClass = Class.forName(objectiveClassName);
		ObjectiveFunction<?> objective = (ObjectiveFunction<?>) objectiveClass.newInstance();
		
		// Initialize the training set.
		String trainingSetClassName = dataConfig.getString("hivemind.data.trainingset.class");
		Class<?> trainingSetClass = Class.forName(trainingSetClassName);
		TrainingSet trainingSet = (TrainingSet) trainingSetClass.newInstance();
		trainingSet.init(dataConfig);
		
		new DroneSystem().init(objective, trainingSet, queenPath);
	}
	
}
