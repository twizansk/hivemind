package twizansk.hivemind.queen;

import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.external.Start;
import twizansk.hivemind.messages.external.Stop;
import twizansk.hivemind.messages.queen.Model;
import twizansk.hivemind.messages.queen.NotReady;
import twizansk.hivemind.messages.queen.Ready;
import twizansk.hivemind.messages.queen.UpdateDone;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class Queen extends UntypedActor {

	private enum State {
		IDLE, ACTIVE
	}

	private final Model model;
	private final ModelUpdater modelUpdater;
	private volatile State state = State.IDLE;

	public Queen(Model model, ModelUpdater modelUpdater) {
		super();
		this.model = model;
		this.modelUpdater = modelUpdater;
	}

	public static Props makeProps(Model model, ModelUpdater modelUpdater) {
		return Props.create(Queen.class, model, modelUpdater);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		// These are the state change messages.  a Start message results in a state of ACTIVE and a Stop message 
		// results in a state of IDLE.  Note that all state changes are idempotent.
		if (msg instanceof Start) {
			state = State.ACTIVE;
			getSender().tell(Ready.instance(), getSelf());
		} else if (msg instanceof Stop) {
			state = State.IDLE;
			getSender().tell(NotReady.instance(), getSelf());
		}
		
		// All other messages are only allowed if the state is ACTIVE
		else if (state.equals(State.IDLE)) {
			getSender().tell(NotReady.instance(), getSelf());
		}
		
		// Here we do the real work.
		else if (msg instanceof UpdateModel) {
			modelUpdater.update(((UpdateModel) msg), model);
			getSender().tell(new UpdateDone(model), getSelf());
		} else {
			unhandled(msg);
		}
	}

}
