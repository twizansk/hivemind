package twizansk.hivemind.drone;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.objective.Gradient;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.messages.drone.FetchNext;
import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.queen.Model;
import twizansk.hivemind.messages.queen.UpdateDone;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Mapper;
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

	private final IObjectiveFunction objectiveFunction;
	private final ActorRef dataFetcher;
	private final ActorSystem system;
	private final static Timeout timeout = new Timeout(Duration.create(5, "seconds"));

	public Drone(IObjectiveFunction objectiveFunction, ITrainingSet trainingSet, ActorSystem system) {
		this.objectiveFunction = objectiveFunction;
		this.system = system;

		// Create supervised actors.
		this.dataFetcher = this.getContext().actorOf(DataFetcher.makeProps(trainingSet));
	}

	public static Props makeProps(IObjectiveFunction objectiveFunction, ITrainingSet trainingSet, ActorSystem system) {
		return Props.create(Drone.class, objectiveFunction, trainingSet, system);
	}

	@Override
	public void onReceive(Object msg) {
		// An UpdateDone message implies that the queen has completed an update
		// to the model and that the next update can be calculated. The
		// UpdateModel object contains the updated model which can now be used
		// by the drone to calculate the next update step.
		if (msg instanceof UpdateDone) {
			Future<UpdateModel> updateModelFuture = this.prepareModelUpdate(((UpdateDone) msg).currentModel);
			Patterns.pipe(updateModelFuture, system.dispatcher()).to(getSender());
		} else
			unhandled(msg);
	}

	/**
	 * Retrieves the next training sample and constructs a future, wrapping the calculation of the next update.
	 * 
	 * @param model
	 * 		The up-to-date model.
	 * @return
	 * 		Future wrapping the update step calculation.
	 */
	private Future<UpdateModel> prepareModelUpdate(final Model model) {
		final Future<Object> trainingSampleFuture = Patterns.ask(dataFetcher, FetchNext.getInstance(), timeout);
		return trainingSampleFuture.map(new Mapper<Object, UpdateModel>() {

			@Override
			public UpdateModel apply(final Object trainingSampeObj) {
				final TrainingSample sample = (TrainingSample) trainingSampeObj;
				return calculateModelUpdate(sample, model);
			}

		}, system.dispatcher());
	}

	/**
	 * Invokes the objective function to calculate the next update step to the model.
	 * 
	 * @param sample
	 * 		The training sample
	 * @param model
	 * 		The up-to-date model.
	 * @return
	 * 		The update step to the model.
	 */
	private UpdateModel calculateModelUpdate(TrainingSample sample, Model model) {
		Gradient gradient = this.objectiveFunction.getGradient(sample, model);
		return MessageFactory.createUpdateModel(gradient);
	}

}
