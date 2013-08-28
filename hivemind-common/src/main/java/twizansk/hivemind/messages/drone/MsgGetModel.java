package twizansk.hivemind.messages.drone;

import java.io.Serializable;

/**
 * {@link MsgGetModel} is used to ask the queen for the current value of the model.
 * 
 * @author twizansk
 *
 */
public final class MsgGetModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final MsgGetModel instance = new MsgGetModel();
	
	public static MsgGetModel instance() {
		return instance;
	}
	
}
