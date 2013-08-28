package twizansk.hivemind.integration;

import twizansk.hivemind.api.objective.ModelFactory;
import twizansk.hivemind.common.Model;

public class IntegrationModelFactory implements ModelFactory {

	@Override
	public Model createModel() {
		return new IntegrationModel();
	}

}
