package twizansk.hivemind.integration;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.objective.Gradient;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.messages.queen.Model;

public class IntegrationObjective implements IObjectiveFunction{

	@Override
	public Gradient getGradient(TrainingSample sample, Model model) {
		IntegrationModel integrationModel = (IntegrationModel) model;
		double[] x = sample.x;
		double y = sample.y;
		double[] params = integrationModel.params;
		double multiplier = 2  * (params[0] * x[0] + params[1] - y);
		double[] g = new double[] {multiplier * x[0], multiplier};
		return new Gradient(g);
	}

}
