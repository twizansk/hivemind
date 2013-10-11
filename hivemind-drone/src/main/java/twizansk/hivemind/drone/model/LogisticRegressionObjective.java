package twizansk.hivemind.drone.model;

import java.util.List;

import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.model.Gradient;
import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.BatchObjectiveFunction;

public class LogisticRegressionObjective implements BatchObjectiveFunction<Model> {

	@Override
	public Gradient singlePointGradient(TrainingSample sample, Model model) {
		double[] X = sample.x;
		double y = sample.y;
		double[] P = model.params;
		double h = sigmoid(X, P);
		double[] g = new double[P.length];
		g[0] = (h - y);
		for (int i = 1; i < P.length; i++) {
			g[i] = (h - y) * X[i-1];
		}
		return new Gradient(g);
	}
	
	private double sigmoid(double[] X, double[] P) {
		// z starts with the offset term.
		double z = P[0];
		
		// We assume all length validations have been done already.
		for (int i = 0; i < X.length; i++) {
			z += X[i] * P[i+1];
		}
		return 1.0 / (1.0 + Math.exp(-z));
	}

	@Override
	public Double objective(List<TrainingSample> samples, Model model) {
		double J = 0.0;
		for (TrainingSample sample : samples) {
			double y = sample.y;
			double[] X = sample.x;
			double[] P = model.params;
			double h = sigmoid(X, P);
			J -= y * Math.log(h) + (1.0 - y * Math.log(1.0 - h));
		}
		return J / samples.size();
	}

}
