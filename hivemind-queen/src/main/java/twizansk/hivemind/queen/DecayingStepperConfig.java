package twizansk.hivemind.queen;

import com.typesafe.config.Config;

import twizansk.hivemind.api.ComponentConfig;

public class DecayingStepperConfig extends ComponentConfig {

	@Override
	protected Object createComponent(Config config) {
		double decayConstant = config.getDouble("hivemind.queen.stepper.decaying.decayConstant");
		return new DecayingStepper(decayConstant);
	}

}
