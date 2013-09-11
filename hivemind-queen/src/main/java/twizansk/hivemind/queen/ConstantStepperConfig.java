package twizansk.hivemind.queen;

import com.typesafe.config.Config;

class ConstantStepperConfig {

	final double stepSize;
	
	public ConstantStepperConfig(Config config) {
		this.stepSize = config.getDouble("hivemind.queen.stepper.constant.stepSize");
	}
	
}
