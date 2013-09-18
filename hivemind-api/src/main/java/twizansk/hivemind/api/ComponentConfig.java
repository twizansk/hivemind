package twizansk.hivemind.api;

import com.typesafe.config.Config;

/**
 * Factory interface for generating hivemind objects from config files.
 * 
 * @author Tommer Wizansky
 * 
 */
public abstract class ComponentConfig  {

	/**
	 * Create a new instance of the target object.
	 * 
	 * @param config
	 *            A Typesafe config object
	 * @return The Hivemind object
	 */
	protected abstract Object createComponent(Config config);

	/**
	 * Use a Typesafe Config object to create a concrete {@link ComponentConfig}.
	 * Then invoke the {@link ComponentConfig} and return an instance of the
	 * target class.
	 * 
	 * @param config
	 * 		A Typesafe config object.
	 * @param key
	 * 		The key for the concrete {@link ComponentConfig} class
	 * @return
	 * 		An instance of the target object.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static Object create(Config config, String key) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String factoryClassName = config.getString(key);
		Class<?> factoryClass = Class.forName(factoryClassName);
		ComponentConfig componentConfig = (ComponentConfig) factoryClass.newInstance();
		return componentConfig.createComponent(config);
	}

}
