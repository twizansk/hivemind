package twizansk.hivemind.messages.drone;

import java.io.Serializable;

public final class MsgStart implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final MsgStart instance = new MsgStart();
	private MsgStart() {}
	
	public static MsgStart instance() {
		return instance;
	}
	
}
