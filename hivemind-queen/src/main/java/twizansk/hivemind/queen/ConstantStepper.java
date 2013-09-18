package twizansk.hivemind.queen;

import twizansk.hivemind.api.model.Stepper;

/**
 * A simple stepper implementation that returns a constant, pre-configured, step size
 * 
 * @author Tommer Wizansky
 *
 */
public class ConstantStepper implements Stepper {

	private final double stepSize;
	
	public ConstantStepper(double stepSize) {
		super();
		this.stepSize = stepSize;
	}

	@Override
	public double getStepSize() {
		return this.stepSize;
	}

	@Override
	public void reset() {
	}

}
