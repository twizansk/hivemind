package twizansk.hivemind.api.model;

import twizansk.hivemind.api.data.TrainingSample;

public interface ObjectiveFunction<T extends Model> {
	
	Gradient getGradient(TrainingSample sample, T model);
	
}
