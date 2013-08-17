package twizansk.hivemind.messages.external;

import java.io.Serializable;

public class Reset implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Reset instance = new Reset();
	
	public static Reset instance() {
		return instance;
	}

}
