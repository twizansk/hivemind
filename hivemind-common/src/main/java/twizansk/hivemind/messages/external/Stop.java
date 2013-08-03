package twizansk.hivemind.messages.external;

import java.io.Serializable;

public class Stop implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Stop instance = new Stop();
	private Stop() {}
	public static Stop instance() {
		return instance;
	}
}
