package twizansk.hivemind.api.data;

public interface ITrainingSet {
	
	/**
	 * Get the next training sample in the set.
	 * 
	 * @return 
	 * 		A trainings sample
	 */
	public TrainingSample getNext();
	
	
	/**
	 * Reset the data set.
	 */
	public void reset();
	
}
