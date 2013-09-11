package twizansk.hivemind.drone.data;

import com.typesafe.config.Config;

/**
 * Configuration properties for the {@link InMemoryCyclicCSVTrainingSet} class
 * 
 * @author Tommer Wizansky
 *
 */
class InMemoryCyclicCSVTrainingSetConfig {

	public final String path;
	
	public InMemoryCyclicCSVTrainingSetConfig(Config config) {
		this.path = config.getString("hivemind.data.trainingset.inmemorycycliccsv.path");
	}
	
}
