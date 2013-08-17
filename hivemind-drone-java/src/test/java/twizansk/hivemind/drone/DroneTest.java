package twizansk.hivemind.drone;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.api.data.ITrainingSet;
import twizansk.hivemind.api.data.TrainingSample;
import twizansk.hivemind.api.objective.Gradient;
import twizansk.hivemind.api.objective.IObjectiveFunction;
import twizansk.hivemind.drone.Drone.State;
import twizansk.hivemind.messages.drone.GetModel;
import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.external.Start;
import twizansk.hivemind.messages.queen.Model;
import twizansk.hivemind.messages.queen.NotReady;
import twizansk.hivemind.messages.queen.UpdateDone;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.testkit.TestActorRef;
import akka.util.Timeout;

@Test(singleThreaded = true)
public class DroneTest {

	public static class TestActor extends UntypedActor {

		public static class Forward implements Serializable {
			private static final long serialVersionUID = 1L;
			public final Object message;
			public final ActorRef target;

			public Forward(Object message, ActorRef target) {
				this.message = message;
				this.target = target;
			}
		}

		public volatile Object lastMessage;

		@Override
		public void onReceive(Object msg) throws Exception {
			if (!(msg instanceof TestActor.Forward)) {
				lastMessage = msg;
			}
			if (msg instanceof Forward) {
				ActorRef target = ((Forward) msg).target;
				Object message = ((Forward) msg).message;
				target.tell(message, getSelf());
			}
		}

	}

	private static class MockTrainingSet implements ITrainingSet {

		private boolean gotSample = false;

		@Override
		public TrainingSample getNext() {
			this.gotSample = true;
			return new TrainingSample(new double[0], 0.0);
		}
		
		@Override
		public void reset() {	
		}

	}

	private static class MockObjectiveFunction implements IObjectiveFunction {

		private boolean gotGradient = false;

		@Override
		public Gradient getGradient(TrainingSample sample, Model model) {
			this.gotGradient = true;
			return new Gradient(null);
		}
	}

	private static Field stateField;

	@BeforeClass
	public void init() throws NoSuchFieldException, SecurityException {
		stateField = Drone.class.getDeclaredField("state");
		stateField.setAccessible(true);
	}

	/**
	 * Check the update model flow, beginning with an UpdateDone message from
	 * the queen.
	 * 
	 * @throws Exception
	 */
	@Test
	public void updateModel() throws Exception {
		ActorSystem system = ActorSystem.create("DroneSystem");
		try {
			MockObjectiveFunction objectiveFunction = new MockObjectiveFunction();
			MockTrainingSet trainingSet = new MockTrainingSet();
			Props props = Drone.makeProps(objectiveFunction, trainingSet, system.deadLetters());
			TestActorRef<Drone> ref = TestActorRef.create(system, props, "testDroneUpdateModel");
			Drone drone = ref.underlyingActor();
			stateField.set(drone, State.ACTIVE);
			Future<Object> future = Patterns.ask(ref, new UpdateDone(new Model()), 3000);
			UpdateModel updateModel = (UpdateModel) Await.result(future, Duration.create(1000, "seconds"));
			Assert.assertNotNull(updateModel);
			Assert.assertTrue(future.isCompleted());
			Assert.assertTrue(objectiveFunction.gotGradient);
			Assert.assertTrue(trainingSet.gotSample);
		} finally {
			system.shutdown();
		}
	}

	/**
	 * Check the rescheduling of messages on receipt of a NotReady from the
	 * queen.
	 * 
	 * @throws Exception
	 */
	public void queenNotReady() throws Exception {
		ActorSystem system = ActorSystem.create("DroneSystem");
		try {
			MockObjectiveFunction objectiveFunction = new MockObjectiveFunction();
			MockTrainingSet trainingSet = new MockTrainingSet();
			Props props = Drone.makeProps(objectiveFunction, trainingSet, system.deadLetters());
			TestActorRef<TestActor> testQueen = TestActorRef.create(system, Props.create(TestActor.class));
			TestActorRef<Drone> ref = TestActorRef.create(system, props, "testDroneUpdateModel");
			Drone drone = ref.underlyingActor();
			stateField.set(drone, State.ACTIVE);

			// Have the queen send a NotReady message wrapping a test message.
			// The test message should be rescheduled for 1 second later.
			testQueen.tell(new TestActor.Forward(new NotReady("Test"), ref), null);

			// After half a second the queen should not have received a message.
			Thread.sleep(500);
			Assert.assertNull(testQueen.underlyingActor().lastMessage);

			// After one and a half a second the queen should not have received
			// the test message.
			Thread.sleep(1000);
			Assert.assertEquals(testQueen.underlyingActor().lastMessage, "Test");
		} finally {
			system.shutdown();
		}
	}

	/**
	 * Check the startup sequence for a drone: IDLE -- (Start) -->
	 * WAITING_FOR_QUEEN -- (Model) --> ACTIVE
	 * 
	 * @throws Exception
	 */
	@Test
	public void startup() throws Exception {
		ActorSystem system = ActorSystem.create("DroneSystem");
		try {
			MockObjectiveFunction objectiveFunction = new MockObjectiveFunction();
			MockTrainingSet trainingSet = new MockTrainingSet();
			TestActorRef<TestActor> testQueen = TestActorRef.create(system, Props.create(TestActor.class));
			Props props = Drone.makeProps(objectiveFunction, trainingSet, testQueen);
			TestActorRef<Drone> ref = TestActorRef.create(system, props, "testDroneStartup");
			Drone drone = ref.underlyingActor();

			// When receiving the Start message, the drone's state should change
			// to WAITING_FOR_QUEEN and it should send a GetModel message to the
			// queen.
			ref.tell(Start.instance(), null);
			Thread.sleep(1000);
			Assert.assertTrue(testQueen.underlyingActor().lastMessage instanceof GetModel);
			Assert.assertEquals(stateField.get(drone), State.WAITING_FOR_QUEEN);

			// Once the queen replies with the current model, the drone's state
			// should change to ACTIVE and an UpdateModel request should be sent
			// to the queen.
			Object updateModel = (UpdateModel) Await.result(Patterns.ask(ref, new Model(), Timeout.longToTimeout(1000)),
					Duration.create(1, TimeUnit.SECONDS));
			Assert.assertTrue(updateModel instanceof UpdateModel);
			Assert.assertEquals(stateField.get(drone), State.ACTIVE);
		} finally {
			system.shutdown();
		}
	}
}
