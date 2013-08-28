package twizansk.hivemind.messages.external;

import java.io.Serializable;

public class MsgReset implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final MsgReset instance = new MsgReset();
	
	public static MsgReset instance() {
		return instance;
	}

}
