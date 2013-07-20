package twizansk.hivemind.messages.drone;

/**
 * {@link GetModel} is used to ask the queen for the current value of the model.
 * 
 * @author twizansk
 *
 */
public final class GetModel {

	private static final GetModel instance = new GetModel();
	
	public static GetModel getInstance() {
		return instance;
	}
	
}
