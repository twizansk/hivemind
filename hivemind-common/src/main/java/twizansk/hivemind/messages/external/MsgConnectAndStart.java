package twizansk.hivemind.messages.external;

import java.io.Serializable;

public class MsgConnectAndStart implements Serializable {
	private static final long serialVersionUID = 1L;
	private static MsgConnectAndStart instance = new MsgConnectAndStart();
	private MsgConnectAndStart() {}
	public static MsgConnectAndStart instance() {
		return instance;
	}
}
