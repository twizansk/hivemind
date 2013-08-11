package twizansk.hivemind.queen;

import twizansk.hivemind.messages.queen.Model;
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
	
	public void init(ModelUpdater modelUpdater, Model model) {
		final ActorSystem system = ActorSystem.create("QueenSystem");
		
		// Create the queen actor.
		system.actorOf(Queen.makeProps(model, modelUpdater), "queen");
	}
	
	public static void main(String[] args) throws Exception {
		Config config = ConfigFactory.load("queen");
		
		String modelUpdaterClassName = config.getString("hivemind.queen.modelUpdater");
		String modelClassName = config.getString("hivemind.queen.model");
		
		Class<?> modelUpdaterClass = modelUpdaterClassName != null ? Class.forName(modelUpdaterClassName) : ModelUpdater.class;		
		Class<?> modelClass = modelClassName != null ? Class.forName(modelClassName) : Model.class;
		ModelUpdater modelUpdater = (ModelUpdater) modelUpdaterClass.newInstance();
		Model model = (Model) modelClass.newInstance();
		new QueenSystem().init(modelUpdater, model);
	}
}
