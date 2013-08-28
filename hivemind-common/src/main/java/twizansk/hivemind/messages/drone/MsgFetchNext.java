package twizansk.hivemind.messages.drone;

import java.io.Serializable;

public final class MsgFetchNext implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final MsgFetchNext instance = new MsgFetchNext();
	
	public static MsgFetchNext instance() {
		return instance;
	}

}
