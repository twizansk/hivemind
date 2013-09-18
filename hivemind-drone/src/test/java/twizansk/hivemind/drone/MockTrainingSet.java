package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.data.TrainingSet;

class MockTrainingSet implements TrainingSet {

	private boolean gotSample = false;
	
	@Override
	public TrainingSample getNext() {
		this.gotSample = true;
		return new TrainingSample(new double[0], 0.0);
	}
	
	@Override
	public void reset() {
	}
	
	public boolean gotSample() {
		return gotSample;
	}

}