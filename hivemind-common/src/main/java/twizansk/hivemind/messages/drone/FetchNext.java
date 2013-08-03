package twizansk.hivemind.messages.drone;

import java.io.Serializable;

public final class FetchNext implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final FetchNext instance = new FetchNext();
	
	public static FetchNext instance() {
		return instance;
	}

}
