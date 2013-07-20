package twizansk.hivemind.api.objective;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.messages.queen.Model;

public interface IObjectiveFunction {
	
	Gradient getGradient(TrainingSample sample, Model model);
	
}
