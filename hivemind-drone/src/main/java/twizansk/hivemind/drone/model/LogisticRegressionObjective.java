package twizansk.hivemind.drone.model;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.model.Gradient;
import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.ObjectiveFunction;

public class LogisticRegressionObjective implements ObjectiveFunction<Model> {

	@Override
	public Gradient getGradient(TrainingSample sample, Model model) {
		double[] X = sample.x;
		double y = sample.y;
		double[] P = model.params;
		double h = sigmoid(X, P);
		double[] g = new double[P.length];
		for (int i = 0; i < P.length; i++) {
			g[i] = (h - y) * X[i];
		}
		return new Gradient(g);
	}
	
	private double sigmoid(double[] X, double[] P) {
		double z = 0.0;
		
		// We assume all length validations have been done already.
		for (int i = 0; i < X.length; i++) {
			z += X[i] * P[i];
		}
		return 1.0 / (1.0 + Math.exp(-z));
	}

}
