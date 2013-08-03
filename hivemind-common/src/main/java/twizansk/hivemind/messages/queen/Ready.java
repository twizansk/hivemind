package twizansk.hivemind.messages.queen;

import java.io.Serializable;

public class Ready implements Serializable {
	private static final long serialVersionUID = 1L;
	private final static Ready instance = new Ready();
	
	private Ready() {}
	
	public static Ready instance() {
		return instance;
	}
}
