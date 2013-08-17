package twizansk.hivemind.integration;

import java.util.Arrays;

import twizansk.hivemind.messages.queen.Model;

public class IntegrationModel extends Model {
	private static final long serialVersionUID = 1L;
	
	public final double[] params = new double[2];
	
	@Override
	public String toString() {
		return Arrays.toString(params);
	}

}
