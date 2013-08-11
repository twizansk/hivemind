package twizansk.hivemind.messages.drone;

import java.io.Serializable;

/**
 * {@link GetModel} is used to ask the queen for the current value of the model.
 * 
 * @author twizansk
 *
 */
public final class GetModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final GetModel instance = new GetModel();
	
	public static GetModel instance() {
		return instance;
	}
	
}
