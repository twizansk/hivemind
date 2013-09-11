package twizansk.hivemind.integration;

import twizansk.hivemind.api.model.ModelUpdater;
import twizansk.hivemind.common.Model;
import twizansk.hivemind.messages.drone.MsgUpdateModel;

public class IntegrationModelUpdater implements ModelUpdater {

	@Override
	public void update(MsgUpdateModel updateModel, Model model, long t, double stepSize) {
		double eta = stepSize / (stepSize + t);
		for (int i = 0; i < model.params.length; i++) {
			model.params[i] -= eta * updateModel.grad[i];
		}
	}
}
