package twizansk.hivemind.drone;

import java.util.concurrent.TimeUnit;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.api.data.EmptyDataSet;
import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.objective.Gradient;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.drone.data.DataFetcher;
import twizansk.hivemind.messages.drone.FetchNext;
import twizansk.hivemind.messages.drone.GetModel;
import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.external.Start;
import twizansk.hivemind.messages.external.Stop;
import twizansk.hivemind.messages.queen.Model;
import twizansk.hivemind.messages.queen.NotReady;
import twizansk.hivemind.messages.queen.UpdateDone;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
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
public final class Drone extends UntypedActor {

	enum State {
		IDLE, ACTIVE, WAITING_FOR_QUEEN
	}

	private final IObjectiveFunction objectiveFunction;
	private final ActorRef dataFetcher;
	private final ActorRef queen;
	private final static Timeout timeout = new Timeout(Duration.create(5, "seconds"));
	private State state = State.IDLE;

	public Drone(IObjectiveFunction objectiveFunction, ITrainingSet trainingSet, ActorRef queen) {
		this.objectiveFunction = objectiveFunction;
		this.queen = queen;

		// Create supervised actors.
		this.dataFetcher = this.getContext().actorOf(DataFetcher.makeProps(trainingSet));
	}

	public static Props makeProps(IObjectiveFunction objectiveFunction, ITrainingSet trainingSet, ActorRef queen) {
		return Props.create(Drone.class, objectiveFunction, trainingSet, queen);
	}

	@Override
	public void onReceive(Object msg) {
		// Start the training.  Ask for the current model from the queen.
		if (msg instanceof Start) {
			this.state = State.WAITING_FOR_QUEEN;
			this.queen.tell(GetModel.instance(), getSelf());
		} else if (msg instanceof Stop) {
			this.state = State.IDLE;
		}

		// If we are waiting for the first communication from the queen, and we
		// got it, change the state to ACTIVE and start processing.
		else if (msg instanceof Model && state.equals(State.WAITING_FOR_QUEEN)) {
			this.state = State.ACTIVE;
			this.prepareModelUpdateAndRespond(((Model) msg), getSender());
		}

		// Any other messages should only be handled when the drone is active.
		else if (!state.equals(State.ACTIVE)) {
			unhandled(msg);
		}
		
		// If the queen is not ready yet, reschedule the message.
		else if (msg instanceof NotReady) {
			getContext().system().scheduler().scheduleOnce(
					Duration.create(1, TimeUnit.SECONDS),
					getSender(), 
					((NotReady) msg).message, 
					getContext().dispatcher(), 
					getSelf());
		}
		
		// An UpdateDone message implies that the queen has completed an update
		// to the model and that the next update can be calculated. The
		// UpdateModel object contains the updated model which can now be used
		// by the drone to calculate the next update step.
		else if (msg instanceof UpdateDone) {
			this.prepareModelUpdateAndRespond(((UpdateDone) msg).currentModel, getSender());
		} else {
			unhandled(msg);
		}
	}

	/**
	 * Retrieves the next training sample, calculates the model update and responds with an {@link UpdateModel} request.
	 * 
	 * @param model
	 *            The up-to-date model.
	 */
	private void prepareModelUpdateAndRespond(final Model model, final ActorRef target) {
		// Get the next training sample from the data set.
		final Future<Object> trainingSampleFuture = Patterns.ask(dataFetcher, FetchNext.instance(), timeout);
		
		// Calculate the model update.
		Future<UpdateModel> future = trainingSampleFuture.map(new Mapper<Object, UpdateModel>() {

			@Override
			public UpdateModel apply(final Object trainingSampeObj) {
				if (trainingSampeObj instanceof EmptyDataSet) {
					throw new RuntimeException("Empty data set");
				}
				
				final TrainingSample sample = (TrainingSample) trainingSampeObj;
				return calculateModelUpdate(sample, model); 
			}

		}, getContext().dispatcher());
		
		// If the data retreival and calculation succeeded, respond to the sender with an update model request.
		future.onSuccess(new OnSuccess<UpdateModel>() {

			@Override
			public void onSuccess(UpdateModel updateModel) throws Throwable {
				target.tell(updateModel, getSelf());
				
			}
		}, getContext().dispatcher());
		
	}

	/**
	 * Invokes the objective function to calculate the next update step to the
	 * model.
	 * 
	 * @param sample
	 *            The training sample
	 * @param model
	 *            The up-to-date model.
	 * @return The update step to the model.
	 */
	private UpdateModel calculateModelUpdate(TrainingSample sample, Model model) {
		Gradient gradient = this.objectiveFunction.getGradient(sample, model);
		return MessageFactory.createUpdateModel(gradient);
	}

}
