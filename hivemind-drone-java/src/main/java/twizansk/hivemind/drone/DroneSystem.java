package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.objective.IObjectiveFunction;
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
	
	public void init(IObjectiveFunction objective, ITrainingSet trainingSet) {
		final ActorSystem system = ActorSystem.create("DroneSystem");
		
		// Create the drone actor.
		system.actorOf(Drone.makeProps(
				objective, 
				trainingSet, 
				system), 
			"drone");
	}
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Config config = ConfigFactory.load("drone");
		Config dataConfig = ConfigFactory.load("data");
		
		String objectiveClassName = config.getString("hivemind.drone.objective");
		String trainingSetClassName = dataConfig.getString("hivemind.data.trainingSet");
		
		Class<?> objectiveClass = Class.forName(objectiveClassName);
		Class<?> trainingSetClass = Class.forName(trainingSetClassName);
		
		IObjectiveFunction objective = (IObjectiveFunction) objectiveClass.newInstance();
		ITrainingSet trainingSet = (ITrainingSet) trainingSetClass.newInstance();
		
		new DroneSystem().init(objective, trainingSet);
	}
	
}
