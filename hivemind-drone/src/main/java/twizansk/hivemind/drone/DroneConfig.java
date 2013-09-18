package twizansk.hivemind.drone;

import twizansk.hivemind.api.ComponentConfig;
import twizansk.hivemind.api.data.TrainingSet;
import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.ObjectiveFunction;
import twizansk.hivemind.common.ActorLookupFactory;
import twizansk.hivemind.common.DefaultActorLookupFactory;

import com.typesafe.config.Config;

/**
 * Configuration for the queen. This includes the concrete implementations of
 * the model updater, model factory, stepper and other configurable
 * components.
 * 
 * @author Tommer Wizansky
 * 
 */
public class DroneConfig {
	
	public final ObjectiveFunction<Model> objectiveFunction; 
	public final TrainingSet trainingSet;
	public final ActorLookupFactory actorLookupFactory;
	
	@SuppressWarnings("unchecked")
	public static DroneConfig createConfig(Config config, Config dataConfig) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// Get the remote path to the queen.
		String queenPath = config.getString("hivemind.queen.path");
		ActorLookupFactory actorLookupFactory = new DefaultActorLookupFactory(queenPath);
		
		// Initialize the objective function.
		String objectiveClassName = config.getString("hivemind.drone.objective");
		Class<?> objectiveClass = Class.forName(objectiveClassName);
		ObjectiveFunction<Model> objective = (ObjectiveFunction<Model>) objectiveClass.newInstance();
		
		// Initialize the training set.
		TrainingSet trainingSet = (TrainingSet) ComponentConfig.create(dataConfig, "hivemind.data.trainingset.config");		
		
		return new DroneConfig(objective, trainingSet, actorLookupFactory);
	}

	public DroneConfig(ObjectiveFunction<Model> objectiveFunction, TrainingSet trainingSet, ActorLookupFactory actorLookupFactory) {
		super();
		this.objectiveFunction = objectiveFunction;
		this.trainingSet = trainingSet;
		this.actorLookupFactory = actorLookupFactory;
	}

	
}
