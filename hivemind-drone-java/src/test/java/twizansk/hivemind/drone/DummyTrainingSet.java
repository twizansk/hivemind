package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.data.TrainingSample;

public class DummyTrainingSet implements ITrainingSet {

	@Override
	public TrainingSample getNext() {
		return new TrainingSample(new double[] {1.0, 2.0}, 3.0);
	}

}
