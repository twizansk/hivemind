package twizansk.hivemind.drone;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.objective.Gradient;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.messages.queen.Model;

public class DummyObjective implements IObjectiveFunction {

	@Override
	public Gradient getGradient(TrainingSample sample, Model model) {
		return null;
	}

}
