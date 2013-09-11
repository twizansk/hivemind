package twizansk.hivemind.queen;

import com.typesafe.config.Config;

import twizansk.hivemind.api.model.Stepper;

public class DecayingStepper implements Stepper {

	private long t = 0;
	private DecayingStepperConfig config;
	
	@Override
	public void init(Config config) {
		this.config = new DecayingStepperConfig(config);
	}

	@Override
	public double getStepSize() {
		return config.alpha / (config.alpha + t++);
	}

	@Override
	public void reset() {
		t = 0;
	}

}
