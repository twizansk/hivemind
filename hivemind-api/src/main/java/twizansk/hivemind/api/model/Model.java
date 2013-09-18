package twizansk.hivemind.api.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A default model implementation.  Contains only and array of parameters.
 * 
 * @author Tommer Wizansky
 *
 */
public class Model implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public final double[] params;
	
	public Model(int nParams) {
		this.params = new double[nParams];
	}

	@Override
	public String toString() {
		return Arrays.toString(params);
	}
}
