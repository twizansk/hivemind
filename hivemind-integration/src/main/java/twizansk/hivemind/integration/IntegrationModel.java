package twizansk.hivemind.integration;

import java.util.Arrays;
import twizansk.hivemind.common.Model;

public class IntegrationModel implements Model {
	private static final long serialVersionUID = 1L;
	
	public final double[] params = new double[2];
	
	@Override
	public String toString() {
		return Arrays.toString(params);
	}

}
