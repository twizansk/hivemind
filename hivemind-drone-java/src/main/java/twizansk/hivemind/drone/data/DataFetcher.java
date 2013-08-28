package twizansk.hivemind.drone.data;

import twizansk.hivemind.api.data.TrainingSet;
import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.messages.drone.MsgFetchNext;
import twizansk.hivemind.messages.external.MsgReset;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class DataFetcher extends UntypedActor {

	private final TrainingSet trainingSet;
	
	public DataFetcher(TrainingSet trainingSet) {
		this.trainingSet = trainingSet;
	}
	
	public static Props makeProps(TrainingSet trainingSet) {
		return Props.create(DataFetcher.class, trainingSet);
	}
	
	@Override
	public void onReceive(Object msg) {
		if (msg instanceof MsgFetchNext) {
			TrainingSample sample = trainingSet.getNext();
			getSender().tell(sample, getSelf());
		} else if (msg instanceof MsgReset) {
			this.trainingSet.reset();
		} else {
			unhandled(msg);
		}
	}
}
