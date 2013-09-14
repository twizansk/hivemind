package twizansk.hivemind.drone;

import java.util.concurrent.TimeUnit;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.api.data.EmptyDataSet;
import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.data.TrainingSet;
import twizansk.hivemind.api.model.Gradient;
import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.MsgUpdateModel;
import twizansk.hivemind.api.model.ObjectiveFunction;
import twizansk.hivemind.common.ActorLookup;
import twizansk.hivemind.common.ActorLookupFactory;
import twizansk.hivemind.common.StateMachine;
import twizansk.hivemind.drone.data.DataFetcher;
import twizansk.hivemind.messages.drone.MsgFetchNext;
import twizansk.hivemind.messages.drone.MsgGetInitialModel;
import twizansk.hivemind.messages.drone.MsgGetModel;
import twizansk.hivemind.messages.external.MsgConnectAndStart;
import twizansk.hivemind.messages.external.MsgReset;
import twizansk.hivemind.messages.external.MsgStop;
import twizansk.hivemind.messages.queen.MsgModel;
import twizansk.hivemind.messages.queen.MsgUpdateDone;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.dispatch.Mapper;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;

/**
 * A Drone is an actor responsible for an atomic unit of work within the total
 * training process. Usually this means a single update step to the model.
 * Drones are meant to be (but don't have to be) colocated with the data they
 * are processing. Consequently, each drone is responsible for a subset of the
 * data.
 * 
 * @author Tommer Wizansky
 * 
 */
public final class Drone extends StateMachine {

	enum State {
		DISCONNECTED, CONNECTING, STOPPED, STARTING, ACTIVE
	}

	private final static Timeout timeout = new Timeout(Duration.create(5, "seconds"));
	private final ObjectiveFunction<Model> objectiveFunction;
	private final ActorRef dataFetcher;
	private final ActorLookup queenLookup;
	
	private ActorRef queen;

	
	//////////////////////////////////////////////
	// Actions
	/////////////////////////////////////////////
	
	private final Action<Drone> CONNECT = new Action<Drone>() {

		@Override
		public void apply(Drone actor, Object message) {
			actor.queenLookup.sendLookup();
			getContext().system().scheduler().scheduleOnce(
					Duration.create(1, TimeUnit.SECONDS),
					getSelf(), 
					MsgConnectAndStart.instance(), 
					getContext().dispatcher(), 
					getSelf());
			
		}
		
	};  
	
	private final Action<Drone> INIT_QUEEN_AND_START = new Action<Drone>() {

		@Override
		public void apply(Drone actor, Object message) {
			if (actor.queenLookup.isTarget((ActorIdentity) message)) {
				actor.queen = ((ActorIdentity) message).getRef();
				actor.getContext().watch(queen);
				GET_INITIAL_MODEL.apply(actor, null);
			}
		}
		
	};  
	
	private final Action<Drone> GET_INITIAL_MODEL = new Action<Drone>() {

		@Override
		public void apply(Drone actor, Object message) {
			actor.queen.tell(MsgGetModel.instance(), getSelf());
			getContext().system().scheduler().scheduleOnce(
					Duration.create(1, TimeUnit.SECONDS),
					getSelf(), 
					MsgGetInitialModel.instance(), 
					getContext().dispatcher(), 
					getSelf());
		}
		
	};  
	
	private final Action<Drone> START_TRAINING = new Action<Drone>() {

		@Override
		public void apply(Drone actor, Object message) {
			actor.prepareModelUpdateAndRespond(((MsgModel) message).model, actor.getSender());
		}
		
	};  
	
	private final Action<Drone> NEXT_UPDATE = new Action<Drone>() {

		@Override
		public void apply(Drone actor, Object message) {
			actor.prepareModelUpdateAndRespond(((MsgUpdateDone) message).currentModel, actor.getSender());
		}
		
	};  
	
	private final Action<Drone> RESET_DATASET = new Action<Drone>() {

		@Override
		public void apply(Drone actor, Object message) {
			actor.dataFetcher.tell(MsgReset.instance(), getSelf());
		}
		
	};  
	
	//////////////////////////////////////////////////////
	// Conditions on transitions
	/////////////////////////////////////////////////////
	
	private final Condition<Drone> IS_QUEEN_IDENTITY = new Condition<Drone>() {

		@Override
		public boolean isSatisfied(Drone actor, Object message) {
			return actor.queenLookup.isTarget((ActorIdentity) message);
		}
		
	};
	
	
	public Drone(ObjectiveFunction<Model> objectiveFunction, TrainingSet trainingSet, ActorLookupFactory actorLookupFactory) {
		this.objectiveFunction = objectiveFunction;
		this.queenLookup = actorLookupFactory.create(this.getContext(), this.getSelf());
		this.state = State.DISCONNECTED;

		// Create supervised actors.
		this.dataFetcher = this.getContext().actorOf(DataFetcher.makeProps(trainingSet));
		
		// Define the state machine
		this.addTransition(State.DISCONNECTED, MsgConnectAndStart.class, new Transition<>(State.CONNECTING, CONNECT));
		this.addTransition(State.CONNECTING, MsgConnectAndStart.class, new Transition<>(State.CONNECTING, CONNECT));
		this.addTransition(State.CONNECTING, ActorIdentity.class, new Transition<>(State.STARTING, INIT_QUEEN_AND_START, IS_QUEEN_IDENTITY));
		this.addTransition(State.STARTING, MsgGetInitialModel.class, new Transition<>(State.STARTING, GET_INITIAL_MODEL));
		this.addTransition(State.STARTING, MsgModel.class, new Transition<>(State.ACTIVE, START_TRAINING));
		this.addTransition(State.STARTING, MsgStop.class, new Transition<>(State.STOPPED));
		this.addTransition(State.ACTIVE, MsgUpdateDone.class, new Transition<>(State.ACTIVE, NEXT_UPDATE));
		this.addTransition(State.ACTIVE, MsgStop.class, new Transition<>(State.STOPPED));
		this.addTransition(State.STOPPED, MsgConnectAndStart.class, new Transition<>(State.STARTING, GET_INITIAL_MODEL));
		this.addTransition(State.STOPPED, MsgReset.class, new Transition<>(State.STOPPED, RESET_DATASET));
		this.addTransition(Terminated.class, new Transition<>(State.CONNECTING, CONNECT));
	}

	public static Props makeProps(ObjectiveFunction<?> objectiveFunction, TrainingSet trainingSet, ActorLookupFactory actorLookupFactory) {
		return Props.create(Drone.class, objectiveFunction, trainingSet, actorLookupFactory);
	}

	/**
	 * Retrieves the next training sample, calculates the model update and responds with an {@link MsgUpdateModel} request.
	 * 
	 * @param model
	 *            The up-to-date model.
	 */
	private void prepareModelUpdateAndRespond(final Model model, final ActorRef target) {
		// Get the next training sample from the data set.
		final Future<Object> trainingSampleFuture = Patterns.ask(dataFetcher, MsgFetchNext.instance(), timeout);
		
		// Calculate the model update.
		Future<MsgUpdateModel> future = trainingSampleFuture.map(new Mapper<Object, MsgUpdateModel>() {

			@Override
			public MsgUpdateModel apply(final Object trainingSampeObj) {
				if (trainingSampeObj instanceof EmptyDataSet) {
					throw new RuntimeException("Empty data set");
				}
				
				final TrainingSample sample = (TrainingSample) trainingSampeObj;
				return calculateModelUpdate(sample, model); 
			}

		}, getContext().dispatcher());
		
		// If the data retrieval and calculation succeeded, respond to the sender with an update model request.
		future.onSuccess(new OnSuccess<MsgUpdateModel>() {

			@Override
			public void onSuccess(MsgUpdateModel updateModel) throws Throwable {
				target.tell(updateModel, getSelf());
				
			}
		}, getContext().dispatcher());
		
	}

	/**
	 * Invokes the objective function to calculate the next update step to the
	 * model.
	 */
	private MsgUpdateModel calculateModelUpdate(TrainingSample sample, Model model) {
		Gradient gradient = this.objectiveFunction.getGradient(sample, model);
		return MessageFactory.createUpdateModel(gradient);
	}
}
