package twizansk.hivemind.api.model;

import twizansk.hivemind.api.data.TrainingSample;

/**
 * {@link TrainingContext} holds user-defined configurations defining the model,
 * objective function and training parameters. The main goal of this class is to
 * enforce type-safety across all calsses that are specific to the model and
 * training scheme.
 * 
 * @author Tommer Wizansky
 * 
 */
public interface TrainingContext<M> {

	/**
	 * Calculates the gradient of the objective function at a
	 * 
	 * @param sample
	 * @param model
	 * @return
	 */
	Gradient calculateGradient(TrainingSample sample, M model);
	
	/**
	 * Create a new instance of the model class.
	 * @return
	 */
	M createNewModel();
	
	

}
