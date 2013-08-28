package twizansk.hivemind.api.data;

public interface TrainingSet {
	
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
