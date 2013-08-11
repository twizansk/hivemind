package twizansk.hivemind.messages.drone;

import java.io.Serializable;

public final class UpdateModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public final double[] grad;

	public UpdateModel(double[] grad) {
		this.grad = grad;
	}
	
}