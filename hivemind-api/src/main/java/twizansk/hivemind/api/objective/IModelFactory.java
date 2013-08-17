package twizansk.hivemind.api.objective;

import twizansk.hivemind.messages.queen.Model;

/**
 * Interface for factory classes that create model objects.
 * 
 * @author Tommer Wizansky
 *
 */
public interface IModelFactory {
	Model createModel();
}
