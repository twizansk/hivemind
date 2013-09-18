package twizansk.hivemind.drone.data;

import twizansk.hivemind.api.ComponentConfig;

import com.typesafe.config.Config;

/**
 * Configuration properties for the {@link InMemoryCyclicCSVTrainingSet} class
 * 
 * @author Tommer Wizansky
 *
 */
public class InMemoryCyclicCSVTrainingSetConfig extends ComponentConfig {

	@Override
	protected Object createComponent(Config config) {
		String path = config.getString("hivemind.data.trainingset.inmemorycycliccsv.path");
		return new InMemoryCyclicCSVTrainingSet(path);
	}
	
}
