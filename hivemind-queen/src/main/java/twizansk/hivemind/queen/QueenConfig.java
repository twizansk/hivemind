package twizansk.hivemind.queen;

import twizansk.hivemind.api.ComponentConfig;
import twizansk.hivemind.api.model.ModelFactory;
import twizansk.hivemind.api.model.ModelUpdater;
import twizansk.hivemind.api.model.Stepper;

import com.typesafe.config.Config;

/**
 * Configuration for the queen. This includes the concrete implementations of
 * the model updater, model factory, stepper and other configurable
 * components.
 * 
 * @author Tommer Wizansky
 * 
 */
public class QueenConfig {

	public final ModelUpdater modelUpdater;
	public final ModelFactory modelFactory;
	public final Stepper stepper;

	public static QueenConfig createConfig(Config config) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String modelUpdaterClassName = config.getString("hivemind.queen.model.updater");
		Class<?> modelUpdaterClass = Class.forName(modelUpdaterClassName);
		ModelUpdater modelUpdater = (ModelUpdater) modelUpdaterClass.newInstance();
		Stepper stepper = (Stepper) ComponentConfig.create(config, "hivemind.queen.stepper.config");
		ModelFactory modelFactory = (ModelFactory) ComponentConfig.create(config, "hivemind.queen.model.factory.config");
		return new QueenConfig(modelUpdater, modelFactory, stepper);
	}

	public QueenConfig(ModelUpdater modelUpdater, ModelFactory modelFactory, Stepper stepper) {
		this.modelUpdater = modelUpdater;
		this.modelFactory = modelFactory;
		this.stepper = stepper;
	}

}
