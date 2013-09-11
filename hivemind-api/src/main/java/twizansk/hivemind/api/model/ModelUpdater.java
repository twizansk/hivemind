package twizansk.hivemind.api.model;

import twizansk.hivemind.common.Model;
import twizansk.hivemind.messages.drone.MsgUpdateModel;

/**
 * Responsible for accepting a model and an {@link MsgUpdateModel} object and updating the model in place.
 * 
 * @author Tommer Wizansky
 *
 */
public interface ModelUpdater {
	
	void update(MsgUpdateModel updateModel, Model model, long t, double stepSize);
	
}
