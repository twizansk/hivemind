package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.data.TrainingSample;

public class DummyTrainingSet implements ITrainingSet {

	@Override
	public TrainingSample getNext() {
		return null;
	}

}
