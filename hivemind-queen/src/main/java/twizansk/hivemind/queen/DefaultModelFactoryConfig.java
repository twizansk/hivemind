package twizansk.hivemind.queen;

import com.typesafe.config.Config;

import twizansk.hivemind.api.ComponentConfig;

public class DefaultModelFactoryConfig extends ComponentConfig {

	@Override
	protected Object createComponent(Config config) {
		int nParams = config.getInt("hivemind.queen.model.nParams");
		return new DefaultModelFactory(nParams);
	}

}
