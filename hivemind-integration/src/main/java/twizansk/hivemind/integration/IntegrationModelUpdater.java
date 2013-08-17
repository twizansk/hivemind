package twizansk.hivemind.integration;

import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.queen.Model;
import twizansk.hivemind.queen.ModelUpdater;

public class IntegrationModelUpdater extends ModelUpdater {

	@Override
	public void update(UpdateModel updateModel, Model model, long t) {
		IntegrationModel integrationModel = (IntegrationModel) model;
		double eta = 10.0 / (10 + t);
		for (int i = 0; i < integrationModel.params.length; i++) {
			integrationModel.params[i] -= eta * updateModel.grad[i];
		}
		System.out.println(integrationModel.toString());
	}
}
