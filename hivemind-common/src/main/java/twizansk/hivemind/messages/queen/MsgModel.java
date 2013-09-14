package twizansk.hivemind.messages.queen;

import java.io.Serializable;

import twizansk.hivemind.api.model.Model;

/**
 * Used to transmit a training model between actors.
 * 
 * @author Tommer Wizansky
 *
 */
public final class MsgModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public final Model model;

	public MsgModel(Model model) {
		super();
		this.model = model;
	}
	
}
