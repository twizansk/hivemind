package twizansk.hivemind.drone;

import twizansk.hivemind.api.ComponentConfig;
import twizansk.hivemind.api.data.TrainingSet;
import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.ObjectiveFunction;

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
	public final String monitorPath;
	public final String queenPath;
	
	@SuppressWarnings("unchecked")
	public static DroneConfig createConfig(Config config) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// Get the remote path to the queen.
		String queenPath = config.getString("hivemind.queen.path");
		String monitorPath = config.getString("hivemind.monitor.path");
		
		// Initialize the objective function.
		String objectiveClassName = config.getString("hivemind.drone.objective");
		Class<?> objectiveClass = Class.forName(objectiveClassName);
		ObjectiveFunction<Model> objective = (ObjectiveFunction<Model>) objectiveClass.newInstance();
		
		// Initialize the training set.
		TrainingSet trainingSet = (TrainingSet) ComponentConfig.create(config, "hivemind.data.trainingset.config");		
		
		return new DroneConfig(objective, trainingSet, queenPath, monitorPath);
	}

	public DroneConfig(
			ObjectiveFunction<Model> objectiveFunction, 
			TrainingSet trainingSet, 
			String queenPath,
			String monitorPath) {
		super();
		this.objectiveFunction = objectiveFunction;
		this.trainingSet = trainingSet;
		this.queenPath = queenPath;
		this.monitorPath = monitorPath;
	}

	
}
