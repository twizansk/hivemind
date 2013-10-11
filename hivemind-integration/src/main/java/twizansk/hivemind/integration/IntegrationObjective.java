package twizansk.hivemind.integration;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.model.Gradient;
import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.ObjectiveFunction;

public class IntegrationObjective implements ObjectiveFunction<Model> {

	@Override
	public Gradient singlePointGradient(TrainingSample sample, Model model) {
		double[] x = sample.x;
		double y = sample.y;
		double[] params = model.params;
		double multiplier = 2  * (params[0] * x[0] + params[1] - y);
		double[] g = new double[] {multiplier * x[0], multiplier};
		return new Gradient(g);
	}

}
