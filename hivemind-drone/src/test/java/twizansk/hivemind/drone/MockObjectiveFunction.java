package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.objective.Gradient;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.common.Model;

class MockObjectiveFunction implements IObjectiveFunction {

	private boolean gotGradient = false;

	@Override
	public Gradient getGradient(TrainingSample sample, Model model) {
		this.gotGradient = true;
		return new Gradient(null);
	}
	
	public boolean gotGradient() {
		return gotGradient;
	}
}