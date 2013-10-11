package twizansk.hivemind.api.model;

import java.util.List;

import twizansk.hivemind.api.data.TrainingSample;

public interface BatchObjectiveFunction<T extends Model> extends ObjectiveFunction<T> {

	Double objective(List<TrainingSample> samples, T model);
	
}
