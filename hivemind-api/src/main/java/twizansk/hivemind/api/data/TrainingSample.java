package twizansk.hivemind.api.data;

import java.io.Serializable;
import java.util.Arrays;

public class TrainingSample implements Serializable {
	private static final long serialVersionUID = 1L;
	public final double[] x;
	public final double y;
	
	public TrainingSample(double[] x, double y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "{x = " + Arrays.toString(x) + ", " + "y = " + y + "}"; 
	}
}
