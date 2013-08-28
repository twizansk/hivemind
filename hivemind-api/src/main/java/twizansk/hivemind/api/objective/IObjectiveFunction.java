package twizansk.hivemind.api.objective;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.common.Model;

public interface IObjectiveFunction {
	
	Gradient getGradient(TrainingSample sample, Model model);
	
}
