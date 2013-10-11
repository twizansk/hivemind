package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.model.Gradient;
import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.ObjectiveFunction;

class MockObjectiveFunction implements ObjectiveFunction<Model> {

	private boolean gotGradient = false;

	@Override
	public Gradient singlePointGradient(TrainingSample sample, Model model) {
		this.gotGradient = true;
		return new Gradient(null);
	}
	
	public boolean gotGradient() {
		return gotGradient;
	}
}