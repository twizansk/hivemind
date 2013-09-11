package twizansk.hivemind.queen;

import twizansk.hivemind.api.model.Stepper;

import com.typesafe.config.Config;

/**
 * A simple stepper implementation that returns a constant, pre-configured, step size
 * 
 * @author Tommer Wizansky
 *
 */
public class ConstantStepper implements Stepper {

	private ConstantStepperConfig config;
	
	@Override
	public double getStepSize() {
		return this.config.stepSize;
	}

	@Override
	public void init(Config config) {
		this.config = new ConstantStepperConfig(config);
	}

	@Override
	public void reset() {
	}

}
