package twizansk.hivemind.queen;

import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.MsgUpdateModel;
import twizansk.hivemind.common.StateMachine;
import twizansk.hivemind.messages.drone.MsgGetModel;
import twizansk.hivemind.messages.external.MsgConnectAndStart;
import twizansk.hivemind.messages.external.MsgStop;
import twizansk.hivemind.messages.queen.MsgModel;
import twizansk.hivemind.messages.queen.MsgNotReady;
import twizansk.hivemind.messages.queen.MsgUpdateDone;
import akka.actor.Props;

public class Queen extends StateMachine {

	enum State {
		IDLE, ACTIVE
	}

	private final QueenConfig config;
	private volatile long t = 1;
	private volatile Model model;

	///////////////////////////////////////////////////////////////////////////////////
	// Actions
	///////////////////////////////////////////////////////////////////////////////////
	
	// Initialize the queen:  reset the time counter and create a new model.
	private Action<Queen> INIT = new Action<Queen>() {

		public void apply(Queen actor, Object message) {
			actor.t = 1;
			actor.model = actor.config.modelFactory.newModel();
		}
	};
	
	// Stop the queen and return a not ready  message to the sender.
	private Action<Queen> STOP = new Action<Queen>() {

		public void apply(Queen actor, Object message) {
			actor.getSender().tell(new MsgNotReady(message), getSelf());
		}
	};
	
	// Return the current model to the sender.
	private Action<Queen> RETURN_MODEL = new Action<Queen>() {

		public void apply(Queen actor, Object message) {
			actor.getSender().tell(new MsgModel(actor.model), getSelf());
		}
	};
	
	// Update the current model with a received update.
	private Action<Queen> UPDATE_MODEL = new Action<Queen>() {

		public void apply(Queen actor, Object message) {
			actor.config.modelUpdater.update(
					((MsgUpdateModel) message), 
					actor.model, 
					actor.t++, 
					actor.config.stepper.getStepSize());
			getSender().tell(new MsgUpdateDone(actor.model), getSelf());
		}
	};
	
	public Queen(QueenConfig config) {
		this.config = config;
		
		// Define the state machine.
		this.state = State.IDLE;
		this.addTransition(State.IDLE, MsgConnectAndStart.class, new Transition<Queen>(State.ACTIVE, INIT));
		this.addTransition(State.ACTIVE, MsgStop.class, new Transition<Queen>(State.IDLE, STOP));
		this.addTransition(State.ACTIVE, MsgGetModel.class, new Transition<Queen>(State.ACTIVE, RETURN_MODEL));
		this.addTransition(State.ACTIVE, MsgUpdateModel.class, new Transition<Queen>(State.ACTIVE, UPDATE_MODEL));
	}

	public static Props makeProps(QueenConfig config) {
		return Props.create(Queen.class, config);
	}

}
