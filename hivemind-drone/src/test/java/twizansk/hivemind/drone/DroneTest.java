package twizansk.hivemind.drone;

import java.lang.reflect.Field;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import scala.Some;
import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.MsgUpdateModel;
import twizansk.hivemind.common.StateMachine;
import twizansk.hivemind.drone.Drone.State;
import twizansk.hivemind.messages.drone.MsgGetInitialModel;
import twizansk.hivemind.messages.drone.MsgGetModel;
import twizansk.hivemind.messages.external.MsgConnectAndStart;
import twizansk.hivemind.messages.external.MsgStop;
import twizansk.hivemind.messages.queen.MsgModel;
import twizansk.hivemind.messages.queen.MsgUpdateDone;
import twizansk.hivemind.test.Initializer;
import twizansk.hivemind.test.MockActor;
import twizansk.hivemind.test.MockActorLookupFactory;
import twizansk.hivemind.test.MockCallback;
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
			Props props = Drone.makeProps(
					new DroneConfig(objectiveFunction, trainingSet, new MockActorLookupFactory(lookupCallback, "akka://QueenSystem/user/testQueen")));
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
							} catch (IllegalArgumentException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						}
					}
				},
				{
					State.STOPPED, 
					State.STARTING, 
					MsgConnectAndStart.instance(),
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
					MsgGetInitialModel.instance(),
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
					new MsgModel(new Model(2)),
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
					State.STOPPED, 
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
					new MsgUpdateDone(new Model(2)),
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
					State.STOPPED, 
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
}
