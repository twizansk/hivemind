package twizansk.hivemind.messages.queen;

import java.io.Serializable;

public class MsgNotReady implements Serializable {
	private static final long serialVersionUID = 1L;
	public final Object message;
	
	public MsgNotReady(Object message) {
		this.message = message;
	}
	
}
