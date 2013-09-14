package twizansk.hivemind.messages.drone;

import java.io.Serializable;

public class MsgGetInitialModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private static MsgGetInitialModel instance = new MsgGetInitialModel();
	private MsgGetInitialModel(){}
	public static MsgGetInitialModel instance() {
		return instance;
	}
	
}
