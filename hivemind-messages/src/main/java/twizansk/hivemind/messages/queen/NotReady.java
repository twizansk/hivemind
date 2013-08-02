package twizansk.hivemind.messages.queen;

import java.io.Serializable;

public class NotReady implements Serializable {
	private static final long serialVersionUID = 1L;
	private final static NotReady instance = new NotReady();
	
	private NotReady() {}
	
	public static NotReady instance() {
		return instance;
	}
}
