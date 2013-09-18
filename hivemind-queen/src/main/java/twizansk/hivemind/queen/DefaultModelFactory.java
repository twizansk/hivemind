package twizansk.hivemind.queen;

import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.ModelFactory;

/**
 * Default implementation of {@link ModelFactory}. Returns a simple
 * {@link Model} object, initialized with a paramter array of the configured
 * size.
 * 
 * @author Tommer Wizansky
 * 
 */
public class DefaultModelFactory implements ModelFactory {

	private final int nParams;

	public DefaultModelFactory(int nParams) {
		super();
		this.nParams = nParams;
	}

	@Override
	public Model newModel() {
		return new Model(nParams);
	}

}
