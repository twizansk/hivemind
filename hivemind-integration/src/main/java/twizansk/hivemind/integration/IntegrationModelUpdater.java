package twizansk.hivemind.integration;

import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.queen.Model;
import twizansk.hivemind.queen.ModelUpdater;

public class IntegrationModelUpdater extends ModelUpdater {

	@Override
	public void update(UpdateModel updateModel, Model model) {
		IntegrationModel integrationModel = (IntegrationModel) model;
		for (int i = 0; i < integrationModel.params.length; i++) {
			integrationModel.params[i] += updateModel.grad[i];
		}
	}
}
