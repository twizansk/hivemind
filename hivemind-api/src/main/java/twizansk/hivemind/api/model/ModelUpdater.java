package twizansk.hivemind.api.model;


/**
 * Responsible for accepting a model and an {@link MsgUpdateModel} object and updating the model in place.
 * 
 * @author Tommer Wizansky
 *
 */
public interface ModelUpdater {
	
	void update(MsgUpdateModel updateModel, Model model, long t, double stepSize);
	
}
