package twizansk.hivemind.messages.drone;

import java.io.Serializable;

public final class MsgUpdateModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public final double[] grad;

	public MsgUpdateModel(double[] grad) {
		this.grad = grad;
	}
	
}