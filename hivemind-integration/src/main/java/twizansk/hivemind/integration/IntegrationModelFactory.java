package twizansk.hivemind.integration;

import twizansk.hivemind.api.objective.IModelFactory;
import twizansk.hivemind.messages.queen.Model;

public class IntegrationModelFactory implements IModelFactory {

	@Override
	public Model createModel() {
		return new IntegrationModel();
	}

}
