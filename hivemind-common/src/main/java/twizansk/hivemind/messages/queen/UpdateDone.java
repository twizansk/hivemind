package twizansk.hivemind.messages.queen;

import java.io.Serializable;

public final class UpdateDone implements Serializable {
	private static final long serialVersionUID = 1L;
	public final Model currentModel;

	public UpdateDone(Model currentModel) {
		super();
		this.currentModel = currentModel;
	}
}
