package twizansk.hivemind.drone;

import org.testng.Assert;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.objective.Gradient;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.queen.Model;
import twizansk.hivemind.messages.queen.UpdateDone;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.TestActorRef;

public class DroneTest {
		
	private static class MockTrainingSet implements ITrainingSet {

		private boolean gotSample = false;
		
		@Override
		public TrainingSample getNext() {
			this.gotSample = true;
			return new TrainingSample(new double[0], 0.0);
		}
		
	}
	
	private static class MockObjectiveFunction implements IObjectiveFunction {

		private boolean gotGradient = false;
		
		@Override
		public Gradient getGradient(TrainingSample sample, Model model) {
			this.gotGradient = true;
			return new Gradient();
		}
	}
	
	@Test
	public void onReceive() throws Exception {
		ActorSystem system = ActorSystem.create("DroneSystem");
		MockObjectiveFunction objectiveFunction = new MockObjectiveFunction();
		MockTrainingSet trainingSet = new MockTrainingSet();
		Props props = Drone.makeProps(
				objectiveFunction, 
				trainingSet, 
				system);
		TestActorRef<Drone> ref = TestActorRef.create(system, props, "testB");
		Future<Object> future = Patterns.ask(ref, new UpdateDone(new Model()), 3000);
		UpdateModel updateModel = (UpdateModel) Await.result(future, Duration.create(1000, "seconds"));
		Assert.assertNotNull(updateModel);
		Assert.assertTrue(future.isCompleted());
		Assert.assertTrue(objectiveFunction.gotGradient);
		Assert.assertTrue(trainingSet.gotSample);
	}
}
