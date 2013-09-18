package twizansk.hivemind.api.model;


/**
 * Classes implementing the Stepper interface are responsible for determining
 * the step size for each model update.
 * 
 * @author Tommer Wizansky
 * 
 */
public interface Stepper {

	/**
	 * Get the step size for the next model update.
	 * 
	 * @return The step size.
	 */
	double getStepSize();

	/**
	 * Initialize the stepper. This method is idempotent and brings the
	 * stepper to the same state regardless of the original state.
	 */
	void reset();

}
