package twizansk.hivemind.drone;

import java.lang.reflect.Field;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import scala.Some;
import twizansk.hivemind.common.StateMachine;
import twizansk.hivemind.drone.Drone.State;
import twizansk.hivemind.messages.drone.MsgGetModel;
import twizansk.hivemind.messages.drone.MsgStart;
import twizansk.hivemind.messages.drone.MsgUpdateModel;
import twizansk.hivemind.messages.external.MsgConnectAndStart;
import twizansk.hivemind.messages.external.MsgStop;
import twizansk.hivemind.messages.queen.MsgModel;
import twizansk.hivemind.messages.queen.MsgUpdateDone;
import twizansk.hivemind.test.Initializer;
import twizansk.hivemind.test.MockActor;
import twizansk.hivemind.test.MockActorLookupFactory;
import twizansk.hivemind.test.MockCallback;
import twizansk.hivemind.test.MockModel;
import twizansk.hivemind.test.Validatable;
import twizansk.hivemind.test.Validator;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.testkit.TestActorRef;

/**
 * Test the state machine transitions for the drone.
 * 
 * @author Tommer Wizansky
 *
 */
@Test(singleThreaded = true)
public class DroneTest {
	
	private static Field stateField;
	private static Field queenField;
	private static Field dataFetcherField;

	@BeforeClass
	public void init() throws NoSuchFieldException, SecurityException {
		stateField = StateMachine.class.getDeclaredField("state");
		queenField = Drone.class.getDeclaredField("queen");
		dataFetcherField = Drone.class.getDeclaredField("dataFetcher");
		stateField.setAccessible(true);
		queenField.setAccessible(true);
		dataFetcherField.setAccessible(true);
	}

	private static class DroneValidatable implements Validatable {
		
		final MockLookupCallback lookupCallback;
		final UntypedActor actor; 
		final TestActorRef<MockActor> sender; 
		final MockTrainingSet trainingSet; 
		final MockObjectiveFunction objectiveFunction;

		DroneValidatable(MockLookupCallback lookupCallback, UntypedActor actor, TestActorRef<MockActor> sender,
				MockTrainingSet trainingSet, MockObjectiveFunction objectiveFunction) {
			super();
			this.lookupCallback = lookupCallback;
			this.actor = actor;
			this.sender = sender;
			this.trainingSet = trainingSet;
			this.objectiveFunction = objectiveFunction;
		}
	}
	
	private static class MockLookupCallback implements MockCallback {
		Boolean executed;
		
		@Override
		public void onExecute(Object... args) {
			executed = true;
		}
		
		Boolean wasExecuted() {
			return executed;
		}
		
	}
	
	@Test(dataProvider="forStateTransition")
	public void stateTransition(
			Object initialState, 
			Object finalState, 
			Object message, 
			TestActorRef<MockActor> sender,
			Initializer initializer, 
			Validator<DroneValidatable> validator) throws Exception {
		MockLookupCallback lookupCallback = new MockLookupCallback();		
		ActorSystem system = ActorSystem.create("DroneSystem");
		try {
			MockObjectiveFunction objectiveFunction = new MockObjectiveFunction();
			MockTrainingSet trainingSet = new MockTrainingSet();
			Props props = Drone.makeProps(objectiveFunction, trainingSet, new MockActorLookupFactory(lookupCallback, "akka://QueenSystem/user/testQueen"));
			TestActorRef<Drone> ref = TestActorRef.create(system, props, "testDroneUpdateModel");
			Drone drone = ref.underlyingActor();
			initializer.init(drone);
			stateField.set(drone, initialState);
			ref.tell(message, sender == null ? system.deadLetters() : sender);
			Thread.sleep(500);
			Assert.assertEquals(stateField.get(drone), finalState);
			validator.validate(new DroneValidatable(lookupCallback, drone, sender, trainingSet, objectiveFunction));
		} finally {
			system.shutdown();
		}
	}
	
	@DataProvider(name="forStateTransition")
	public Object[][] forStateTransition() {		
		return new Object[][] {
				{
					State.DISCONNECTED, 
					State.CONNECTING, 
					MsgConnectAndStart.instance(),
					null,
					new Initializer() {
						public void init(UntypedActor actor) throws Exception {
						}
					},
					new Validator<DroneValidatable>() {
						public void validate(DroneValidatable validatable) {
							Assert.assertTrue(validatable.lookupCallback.wasExecuted());
						}
					}
				},
				{
					State.CONNECTING, 
					State.CONNECTING, 
					MsgConnectAndStart.instance(),
					null,
					new Initializer() {
						public void init(UntypedActor actor) throws Exception {
						}
					},
					new Validator<DroneValidatable>() {
						public void validate(DroneValidatable validatable) {
							Assert.assertTrue(validatable.lookupCallback.wasExecuted());
						}
					}
				},
				{
					State.CONNECTING, 
					State.STARTING, 
					new ActorIdentity(null, new Some<ActorRef>(
							TestActorRef.create(ActorSystem.create("QueenSystem"), MockActor.makeProps(), "testQueen"))),
					null,
					new Initializer() {
						public void init(UntypedActor actor) throws Exception {
							queenField.set(actor, null);
						}
					},
					new Validator<DroneValidatable>() {
						@SuppressWarnings("unchecked")
						public void validate(DroneValidatable validatable) {
							try {
								MockActor queen = ((TestActorRef<MockActor>) queenField.get(validatable.actor)).underlyingActor();
								Assert.assertNotNull(queen);
								Assert.assertEquals(queen.getLastMessage(), MsgGetModel.instance());
								
							} catch (IllegalArgumentException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						}
					}
				},
				{
					State.STARTING, 
					State.STARTING, 
					MsgStart.instance(),
					null,
					new Initializer() {
						public void init(UntypedActor actor) throws Exception {
							queenField.set(actor, TestActorRef.create(ActorSystem.create("QueenSystem"), MockActor.makeProps(), "testQueen"));
						}
					},
					new Validator<DroneValidatable>() {
						@SuppressWarnings("unchecked")
						public void validate(DroneValidatable validatable) {
							try {
								MockActor queen = ((TestActorRef<MockActor>) queenField.get(validatable.actor)).underlyingActor();
								Assert.assertEquals(queen.getLastMessage(), MsgGetModel.instance());
								
							} catch (IllegalArgumentException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						}
					}
				},
				{
					State.STARTING, 
					State.ACTIVE, 
					new MsgModel(new MockModel()),
					TestActorRef.create(ActorSystem.create("QueenSystem"), MockActor.makeProps(), "testQueen"),
					new Initializer() {
						public void init(UntypedActor actor) throws Exception {
						}
					},
					new Validator<DroneValidatable>() {
						public void validate(DroneValidatable validatable) {
							MockActor queen = validatable.sender.underlyingActor();
							Assert.assertEquals(queen.getLastMessage().getClass(), MsgUpdateModel.class);
							Assert.assertTrue(validatable.trainingSet.gotSample());
							Assert.assertTrue(validatable.objectiveFunction.gotGradient());
						}
					}
				},
				{
					State.STARTING, 
					State.IDLE, 
					MsgStop.instance(),
					null,
					new Initializer() {
						public void init(UntypedActor actor) throws Exception {
						}
					},
					new Validator<DroneValidatable>() {
						public void validate(DroneValidatable validatable) {
						}
					}
				},
				{
					State.ACTIVE, 
					State.ACTIVE, 
					new MsgUpdateDone(new MockModel()),
					TestActorRef.create(ActorSystem.create("QueenSystem"), MockActor.makeProps(), "testQueen"),
					new Initializer() {
						public void init(UntypedActor actor) throws Exception {
						}
					},
					new Validator<DroneValidatable>() {
						public void validate(DroneValidatable validatable) {
							MockActor queen = validatable.sender.underlyingActor();
							Assert.assertEquals(queen.getLastMessage().getClass(), MsgUpdateModel.class);
							Assert.assertTrue(validatable.trainingSet.gotSample());
							Assert.assertTrue(validatable.objectiveFunction.gotGradient());
						}
					}
				},
				{
					State.ACTIVE, 
					State.IDLE, 
					MsgStop.instance(),
					null,
					new Initializer() {
						public void init(UntypedActor actor) throws Exception {
						}
					},
					new Validator<DroneValidatable>() {
						public void validate(DroneValidatable validatable) {
						}
					}
				},
		};
	}

	
//	/**
//	 * Check the update model flow, beginning with an UpdateDone message from
//	 * the queen.
//	 * 
//	 * @throws Exception
//	 */
//	@Test
//	public void updateModel() throws Exception {
//		ActorSystem system = ActorSystem.create("DroneSystem");
//		try {
//			MockObjectiveFunction objectiveFunction = new MockObjectiveFunction();
//			MockTrainingSet trainingSet = new MockTrainingSet();
//			Props props = Drone.makeProps(objectiveFunction, trainingSet, system.deadLetters());
//			TestActorRef<Drone> ref = TestActorRef.create(system, props, "testDroneUpdateModel");
//			Drone drone = ref.underlyingActor();
//			stateField.set(drone, State.ACTIVE);
//			Future<Object> future = Patterns.ask(ref, new MsgUpdateDone(new MockModel()), 3000);
//			MsgUpdateModel updateModel = (MsgUpdateModel) Await.result(future, Duration.create(1000, "seconds"));
//			Assert.assertNotNull(updateModel);
//			Assert.assertTrue(future.isCompleted());
//			Assert.assertTrue(objectiveFunction.gotGradient);
//			Assert.assertTrue(trainingSet.gotSample);
//		} finally {
//			system.shutdown();
//		}
//	}
//
//	/**
//	 * Check the rescheduling of messages on receipt of a NotReady from the
//	 * queen.
//	 * 
//	 * @throws Exception
//	 */
//	public void queenNotReady() throws Exception {
//		ActorSystem system = ActorSystem.create("DroneSystem");
//		try {
//			MockObjectiveFunction objectiveFunction = new MockObjectiveFunction();
//			MockTrainingSet trainingSet = new MockTrainingSet();
//			Props props = Drone.makeProps(objectiveFunction, trainingSet, system.deadLetters());
//			TestActorRef<TestActor> testQueen = TestActorRef.create(system, Props.create(TestActor.class));
//			TestActorRef<Drone> ref = TestActorRef.create(system, props, "testDroneUpdateModel");
//			Drone drone = ref.underlyingActor();
//			stateField.set(drone, State.ACTIVE);
//
//			// Have the queen send a NotReady message wrapping a test message.
//			// The test message should be rescheduled for 1 second later.
//			testQueen.tell(new TestActor.Forward(new MsgNotReady("Test"), ref), null);
//
//			// After half a second the queen should not have received a message.
//			Thread.sleep(500);
//			Assert.assertNull(testQueen.underlyingActor().lastMessage);
//
//			// After one and a half a second the queen should not have received
//			// the test message.
//			Thread.sleep(1000);
//			Assert.assertEquals(testQueen.underlyingActor().lastMessage, "Test");
//		} finally {
//			system.shutdown();
//		}
//	}
//
//	/**
//	 * Check the startup sequence for a drone: IDLE -- (Start) -->
//	 * WAITING_FOR_QUEEN -- (Model) --> ACTIVE
//	 * 
//	 * @throws Exception
//	 */
//	@Test
//	public void startup() throws Exception {
//		ActorSystem system = ActorSystem.create("DroneSystem");
//		try {
//			MockObjectiveFunction objectiveFunction = new MockObjectiveFunction();
//			MockTrainingSet trainingSet = new MockTrainingSet();
//			TestActorRef<TestActor> testQueen = TestActorRef.create(system, Props.create(TestActor.class));
//			Props props = Drone.makeProps(objectiveFunction, trainingSet, testQueen);
//			TestActorRef<Drone> ref = TestActorRef.create(system, props, "testDroneStartup");
//			Drone drone = ref.underlyingActor();
//
//			// When receiving the Start message, the drone's state should change
//			// to WAITING_FOR_QUEEN and it should send a GetModel message to the
//			// queen.
//			ref.tell(MsgConnectAndStart.instance(), null);
//			Thread.sleep(1000);
//			Assert.assertTrue(testQueen.underlyingActor().lastMessage instanceof MsgGetModel);
//			Assert.assertEquals(stateField.get(drone), State.CONNECTING);
//
//			// Once the queen replies with the current model, the drone's state
//			// should change to ACTIVE and an UpdateModel request should be sent
//			// to the queen.
//			Object updateModel = (MsgUpdateModel) Await.result(Patterns.ask(ref, new MockModel(), Timeout.longToTimeout(1000)),
//					Duration.create(1, TimeUnit.SECONDS));
//			Assert.assertTrue(updateModel instanceof MsgUpdateModel);
//			Assert.assertEquals(stateField.get(drone), State.ACTIVE);
//		} finally {
//			system.shutdown();
//		}
//	}
}
