package twizansk.hivemind.drone;

import twizansk.hivemind.api.objective.Gradient;
import twizansk.hivemind.messages.drone.UpdateModel;

public class MessageFactory {

	public static UpdateModel createUpdateModel(Gradient gradient) {
		return new UpdateModel(); // TODO: replace with real implementation.
	}
	
}
