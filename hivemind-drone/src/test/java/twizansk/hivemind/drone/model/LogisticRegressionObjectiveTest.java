package twizansk.hivemind.drone.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.model.Gradient;
import twizansk.hivemind.api.model.Model;

public class LogisticRegressionObjectiveTest {

	@Test
	public void getGradient() {
		Model model = new Model(3);
		model.params[0] = 0.2;
		model.params[1] = 0.3;
		model.params[2] = 0.4;
		
		LogisticRegressionObjective objective = new LogisticRegressionObjective();
		Gradient g = objective.singlePointGradient(new TrainingSample(new double[]{1.0, 2.0}, 3.0), model);
		double a = (0.7858 - 3.0);
		double[] expected = new double[] {a, a * 1.0, a * 2.0};
		for (int i = 0; i < 3; i++) {
			Assert.assertEquals(g.grad[i], expected[i], 0.001);
		}
	}
}
