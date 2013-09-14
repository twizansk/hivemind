package twizansk.hivemind.messages.queen;

import java.io.Serializable;

import twizansk.hivemind.api.model.Model;

public final class MsgUpdateDone implements Serializable {
	private static final long serialVersionUID = 1L;
	public final Model currentModel;

	public MsgUpdateDone(Model currentModel) {
		super();
		this.currentModel = currentModel;
	}
}
