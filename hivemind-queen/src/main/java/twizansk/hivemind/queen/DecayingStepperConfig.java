package twizansk.hivemind.queen;

import com.typesafe.config.Config;

class DecayingStepperConfig {

	final double alpha;
	
	public DecayingStepperConfig(Config config) {
		this.alpha = config.getDouble("hivemind.queen.stepper.decaying.alpha");
	}
	
}
