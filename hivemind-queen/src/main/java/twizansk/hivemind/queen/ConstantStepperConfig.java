package twizansk.hivemind.queen;

import twizansk.hivemind.api.ComponentConfig;

import com.typesafe.config.Config;

public class ConstantStepperConfig extends ComponentConfig {

	@Override
	protected Object createComponent(Config config) {
		double stepSize = config.getDouble("hivemind.queen.stepper.constant.stepSize");
		return new ConstantStepper(stepSize);
	}
	
}
