package twizansk.hivemind.api.model;

import twizansk.hivemind.api.data.TrainingSample;

public interface ObjectiveFunction<T extends Model> {
	
	Gradient singlePointGradient(TrainingSample sample, T model);
	
}
