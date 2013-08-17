package twizansk.hivemind.queen;

import twizansk.hivemind.api.objective.IModelFactory;
import twizansk.hivemind.messages.drone.GetModel;
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

	enum State {
		IDLE, ACTIVE
	}

	private final IModelFactory modelFactory;
	private final ModelUpdater modelUpdater;
	private volatile State state = State.IDLE;
	private volatile long t = 1;
	private volatile Model model;

	public Queen(IModelFactory modelFactory, ModelUpdater modelUpdater) {
		super();
		this.modelFactory = modelFactory;
		this.modelUpdater = modelUpdater;
	}

	public static Props makeProps(IModelFactory modelFactory, ModelUpdater modelUpdater) {
		return Props.create(Queen.class, modelFactory, modelUpdater);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		// These are the state change messages.  a Start message results in a state of ACTIVE and a Stop message 
		// results in a state of IDLE.  Note that all state changes are idempotent.
		if (msg instanceof Start) {
			this.state = State.ACTIVE;
			this.t = 1;
			this.model = modelFactory.createModel();
			getSender().tell(Ready.instance(), getSelf());
		} else if (msg instanceof Stop) {
			this.state = State.IDLE;
			getSender().tell(new NotReady(msg), getSelf());
		}
		
		// All other messages are only allowed if the state is ACTIVE
		else if (this.state.equals(State.IDLE)) {
			getSender().tell(new NotReady(msg), getSelf());
		}
		
		// Here we do the real work.
		else if (msg instanceof GetModel) {
			getSender().tell(model, getSelf());
		}
		else if (msg instanceof UpdateModel) {
			this.modelUpdater.update(((UpdateModel) msg), this.model, this.t++);
			getSender().tell(new UpdateDone(this.model), getSelf());
		} else {
			unhandled(msg);
		}
	}

}
