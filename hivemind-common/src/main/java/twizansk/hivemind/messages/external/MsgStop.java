package twizansk.hivemind.messages.external;

import java.io.Serializable;

public class MsgStop implements Serializable {
	private static final long serialVersionUID = 1L;
	private static MsgStop instance = new MsgStop();
	private MsgStop() {}
	public static MsgStop instance() {
		return instance;
	}
}
