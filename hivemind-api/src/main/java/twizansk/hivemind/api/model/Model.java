package twizansk.hivemind.api.model;

import java.io.Serializable;
import java.util.Arrays;

public class Model implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public final double[] params = new double[2];
	
	@Override
	public String toString() {
		return Arrays.toString(params);
	}
}
