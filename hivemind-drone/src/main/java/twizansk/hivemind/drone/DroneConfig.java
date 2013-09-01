package twizansk.hivemind.drone;

import twizansk.hivemind.api.objective.IObjectiveFunction;

/**
 * Configuration class for Drone systems.
 * 
 * @author Tommer Wizansky
 *
 */
public final class DroneConfig {

	public final IObjectiveFunction objectiveFunction;
	
	public DroneConfig() {
		this.objectiveFunction = null; // TODO: read from file.
	}
	
	
	
}
