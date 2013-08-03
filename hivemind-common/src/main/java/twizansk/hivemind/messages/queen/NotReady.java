package twizansk.hivemind.messages.queen;

import java.io.Serializable;

public class NotReady implements Serializable {
	private static final long serialVersionUID = 1L;
	public final Object message;
	
	public NotReady(Object message) {
		this.message = message;
	}
	
}
