package twizansk.hivemind.queen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twizansk.hivemind.api.model.ModelUpdater;
import twizansk.hivemind.common.Model;
import twizansk.hivemind.messages.drone.MsgUpdateModel;

public class DefaultModelUpdater implements ModelUpdater {

	private final static Logger LOG = LoggerFactory.getLogger(DefaultModelUpdater.class);
	
	@Override
	public void update(MsgUpdateModel updateModel, Model model, long t, double stepSize) {
		for (int i = 0; i < model.params.length; i++) {
			model.params[i] -= stepSize * updateModel.grad[i];
		}
		
		LOG.debug("model: " + model.toString());
	}

}
