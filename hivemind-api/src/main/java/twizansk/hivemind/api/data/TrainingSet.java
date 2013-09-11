package twizansk.hivemind.api.data;

import twizansk.hivemind.api.Configurable;

import com.typesafe.config.Config;

public interface TrainingSet extends Configurable {
	
	/**
	 * Initialize the training set from a configuration. 
	 */
	public void init(Config config);
	
	/**
	 * Get the next training sample in the set.
	 * 
	 * @return 
	 * 		A training sample
	 */
	public TrainingSample getNext();
	
	
	/**
	 * Reset the data set.
	 */
	public void reset();
	
}
