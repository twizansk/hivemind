package twizansk.hivemind.queen;

import twizansk.hivemind.api.model.ModelUpdater;
import twizansk.hivemind.api.model.Stepper;
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
	
	public void init(ModelUpdater modelUpdater, Stepper stepper) {
		final ActorSystem system = ActorSystem.create("QueenSystem");
		
		// Create the queen actor.
		system.actorOf(Queen.makeProps(modelUpdater, stepper), "queen");
	}
	
	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load("queen");
		
		String modelUpdaterClassName = config.getString("hivemind.queen.modelUpdater");
		String stepperClassName = config.getString("hivemind.queen.stepper.class");
		
		Class<?> modelUpdaterClass = modelUpdaterClassName != null ? Class.forName(modelUpdaterClassName) : ModelUpdater.class;		
		Class<?> stepperClass = Class.forName(stepperClassName);
		
		ModelUpdater modelUpdater = (ModelUpdater) modelUpdaterClass.newInstance();
		Stepper stepper = (Stepper) stepperClass.newInstance();
		stepper.init(config);
		
		new QueenSystem().init(modelUpdater, stepper);
	}
}
