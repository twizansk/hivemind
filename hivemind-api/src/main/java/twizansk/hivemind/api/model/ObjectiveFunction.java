package twizansk.hivemind.api.model;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.common.Model;

public interface ObjectiveFunction<T extends Model> {
	
	Gradient getGradient(TrainingSample sample, T model);
	
}
