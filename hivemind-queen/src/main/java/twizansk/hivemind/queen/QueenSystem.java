package twizansk.hivemind.queen;

import twizansk.hivemind.api.objective.ModelFactory;
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
	
	public void init(ModelUpdater modelUpdater, ModelFactory modelFactory) {
		final ActorSystem system = ActorSystem.create("QueenSystem");
		
		// Create the queen actor.
		system.actorOf(Queen.makeProps(modelFactory, modelUpdater), "queen");
	}
	
	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load("queen");
		
		String modelUpdaterClassName = config.getString("hivemind.queen.modelUpdater");
		String modelFactoryClassName = config.getString("hivemind.queen.modelFactory");
		
		Class<?> modelUpdaterClass = modelUpdaterClassName != null ? Class.forName(modelUpdaterClassName) : ModelUpdater.class;		
		Class<?> modelFactoryClass = Class.forName(modelFactoryClassName);
		ModelUpdater modelUpdater = (ModelUpdater) modelUpdaterClass.newInstance();
		ModelFactory modelFactory = (ModelFactory) modelFactoryClass.newInstance();
		new QueenSystem().init(modelUpdater, modelFactory);
	}
}
