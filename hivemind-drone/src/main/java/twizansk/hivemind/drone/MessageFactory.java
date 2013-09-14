package twizansk.hivemind.drone;

import twizansk.hivemind.api.model.Gradient;
import twizansk.hivemind.api.model.MsgUpdateModel;

public class MessageFactory {

	public static MsgUpdateModel createUpdateModel(Gradient gradient) {
		return new MsgUpdateModel(gradient.grad); // TODO: replace with real implementation.
	}
	
}
