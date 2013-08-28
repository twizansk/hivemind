package twizansk.hivemind.messages.queen;

import java.io.Serializable;

public final class MsgReady implements Serializable {
	private static final long serialVersionUID = 1L;
	private final static MsgReady instance = new MsgReady();
	
	private MsgReady() {}
	
	public static MsgReady instance() {
		return instance;
	}
}
