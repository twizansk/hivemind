package twizansk.hivemind.messages.drone;

import java.io.Serializable;
import java.util.Date;

import twizansk.hivemind.api.data.TrainingSample;

/**
 * Holds a time-stamped training sample
 * 
 * @author Tommer Wizansky
 *
 */
public class MsgTrainingSample implements Serializable {
	private static final long serialVersionUID = 1L;
	public final Date timeStamp;
	public final TrainingSample trainingSample;

	public MsgTrainingSample(Date timeStamp, TrainingSample trainingSample) {
		this.timeStamp = timeStamp;
		this.trainingSample = trainingSample;
	}
	
}
