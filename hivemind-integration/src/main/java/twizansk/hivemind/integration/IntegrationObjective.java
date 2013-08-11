package twizansk.hivemind.integration;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.objective.Gradient;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.messages.queen.Model;

public class IntegrationObjective implements IObjectiveFunction{

	@Override
	public Gradient getGradient(TrainingSample sample, Model model) {
		IntegrationModel integrationModel = (IntegrationModel) model;
		double[] g = new double[integrationModel.params.length];
		for (int i = 0; i < g.length; i++) {
			g[i] = 2 * integrationModel.params[i] + 1;
		}
		return new Gradient(g);
	}

}
