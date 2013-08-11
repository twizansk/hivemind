package twizansk.hivemind.integration;

import twizansk.hivemind.api.data.EmptyDataSet;
import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.data.TrainingSample;

public class IntegrationTrainingSet implements ITrainingSet {

	private volatile int n;
	
	@Override
	public TrainingSample getNext() {
		if (n == 3) {
			return new EmptyDataSet();
		}
		n++;
		return new TrainingSample(new double[] {1.0, 2.0}, 3.0);
	}

}
