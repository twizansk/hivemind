package twizansk.hivemind.queen;

import twizansk.hivemind.api.model.Stepper;

public class DecayingStepper implements Stepper {

	private long t = 0;
	private final double decayConstant;
	

	public DecayingStepper(double alpha) {
		this.decayConstant = alpha;
	}

	@Override
	public double getStepSize() {
		return decayConstant / (decayConstant + t++);
	}

	@Override
	public void reset() {
		t = 0;
	}

}
