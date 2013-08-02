package twizansk.hivemind.messages.external;

import java.io.Serializable;

public class Start implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Start instance = new Start();
	private Start() {}
	public static Start instance() {
		return instance;
	}
}
