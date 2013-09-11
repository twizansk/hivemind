package twizansk.hivemind.api;

import com.typesafe.config.Config;

/**
 * Interface for classes that are configured from a Typesafe {@link Config} instance.
 * 
 * @author Tommer Wizansky
 *
 */
public interface Configurable {

	/**
	 * Apply the configuration.
	 * 
	 * @param config
	 * 		The configuration.
	 */
	void init(Config config);
	
}
