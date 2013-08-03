package twizansk.hivemind.drone;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.common.ActorLookup;
import twizansk.hivemind.messages.external.Start;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Initializes the actor system for a drone node.
 * 
 * @author Tommer Wizansky
 *
 */
public class DroneSystem {
		
	public void init(IObjectiveFunction objective, ITrainingSet trainingSet, String queenPath) {
		final ActorSystem system = ActorSystem.create("DroneSystem");

		// Look up the queen.
		ActorRef queenLookup = system.actorOf(ActorLookup.makeProps(queenPath), "queenLookup");
		Future<Object> future = Patterns.ask(queenLookup, ActorLookup.GET_ACTOR_REF, null);
		ActorRef queen;
		try {
			queen = (ActorRef) Await.result(future, Duration.Inf()); 
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
				
		// Create the drone actor and start it.
		ActorRef drone = system.actorOf(Drone.makeProps(
				objective, 
				trainingSet, 
				queen), 
			"drone");
		drone.tell(Start.instance(), null);
	}
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Config config = ConfigFactory.load("drone");
		Config dataConfig = ConfigFactory.load("data");
		
		String objectiveClassName = config.getString("hivemind.drone.objective");
		String trainingSetClassName = dataConfig.getString("hivemind.data.trainingSet");
		String queenPath = config.getString("hivemind.queen.path");
		
		Class<?> objectiveClass = Class.forName(objectiveClassName);
		Class<?> trainingSetClass = Class.forName(trainingSetClassName);
		
		IObjectiveFunction objective = (IObjectiveFunction) objectiveClass.newInstance();
		ITrainingSet trainingSet = (ITrainingSet) trainingSetClass.newInstance();
		
		new DroneSystem().init(objective, trainingSet, queenPath);
	}
	
}
