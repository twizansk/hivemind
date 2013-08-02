package twizansk.hivemind.drone.data;

import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.messages.drone.FetchNext;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class DataFetcher extends UntypedActor {

	private final ITrainingSet trainingSet;
	
	public DataFetcher(ITrainingSet trainingSet) {
		this.trainingSet = trainingSet;
	}
	
	public static Props makeProps(ITrainingSet trainingSet) {
		return Props.create(DataFetcher.class, trainingSet);
	}
	
	@Override
	public void onReceive(Object msg) {
		if (msg instanceof FetchNext) {
			TrainingSample sample = trainingSet.getNext();
			getSender().tell(sample, getSelf());
		} else {
			unhandled(msg);
		}
	}
}
