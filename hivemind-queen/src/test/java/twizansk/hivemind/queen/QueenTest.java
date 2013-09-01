package twizansk.hivemind.queen;

import java.lang.reflect.Field;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import twizansk.hivemind.api.objective.ModelFactory;
import twizansk.hivemind.common.Model;
import twizansk.hivemind.common.StateMachine;
import twizansk.hivemind.messages.drone.MsgGetModel;
import twizansk.hivemind.messages.drone.MsgUpdateModel;
import twizansk.hivemind.messages.external.MsgConnectAndStart;
import twizansk.hivemind.messages.external.MsgStop;
import twizansk.hivemind.messages.queen.MsgModel;
import twizansk.hivemind.messages.queen.MsgNotReady;
import twizansk.hivemind.messages.queen.MsgUpdateDone;
import twizansk.hivemind.queen.Queen.State;
import twizansk.hivemind.test.Initializer;
import twizansk.hivemind.test.MockActor;
import twizansk.hivemind.test.Validatable;
import twizansk.hivemind.test.Validator;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.testkit.TestActorRef;

/**
 * Test state machine transitions for queen.
 * 
 * @author Tommer Wizansky
 *
 */
@Test(singleThreaded=true)
public class QueenTest {

	class MockModelUpdater extends ModelUpdater {
		boolean updated = false;

		@Override
		public void update(MsgUpdateModel updateModel, Model model, long t) {
			super.update(updateModel, model, t);
			this.updated = true;
		}
	}
	
	class MockModel implements Model {
		private static final long serialVersionUID = 1L;
	}

	private class QueenValidatable implements Validatable {
		final Queen queen;
		final TestActorRef<MockActor> sender;
		final MockModelUpdater modelUpdater;

		public QueenValidatable(Queen queen, TestActorRef<MockActor> sender, MockModelUpdater modelUpdater) {
			super();
			this.queen = queen;
			this.sender = sender;
			this.modelUpdater = modelUpdater;
		}
	}
	
	private static Field stateField;
	private static Field tField;
	private static Field modelField;
	
	@BeforeClass
	public void init() throws NoSuchFieldException, SecurityException {
		stateField = StateMachine.class.getDeclaredField("state");
		tField = Queen.class.getDeclaredField("t");
		modelField = Queen.class.getDeclaredField("model");
		stateField.setAccessible(true);
		tField.setAccessible(true);
		modelField.setAccessible(true);
	}
	
	@Test(dataProvider="forStateTransition")
	public void stateTransition(
			Object initialState, 
			Object finalState, 
			Object message, 
			TestActorRef<MockActor> sender,
			Initializer initializer, 
			Validator<QueenValidatable> validator) throws Exception {
		
		
		ActorSystem system = ActorSystem.create("DroneSystem");
		try {
			MockModelUpdater modelUpdater = new MockModelUpdater();
			Props props = Queen.makeProps(new ModelFactory() {
				public Model createModel() {
					return new MockModel();
				}
			}, modelUpdater);
			TestActorRef<Queen> ref = TestActorRef.create(system, props, "testQueen");
			Queen queen = ref.underlyingActor();
			initializer.init(queen);
			stateField.set(queen, initialState);
			ref.tell(message, sender == null ? system.deadLetters() : sender);
			Thread.sleep(500);
			Assert.assertEquals(stateField.get(queen), finalState);
			validator.validate(new QueenValidatable(queen, sender, modelUpdater));
		} finally {
			system.shutdown();
		}
	}

	@DataProvider(name="forStateTransition")
	public Object[][] forStateTransition() throws Exception {		
		return new Object[][] {
			{
				State.IDLE, 
				State.ACTIVE, 
				MsgConnectAndStart.instance(),
				null,
				new Initializer() {
					public void init(UntypedActor actor) throws Exception {
						tField.set(actor, 0);
						modelField.set(actor, null);
					}
				},
				new Validator<QueenValidatable>() {
					public void validate(QueenValidatable validatable) {
						try {
							Assert.assertEquals(tField.get(validatable.queen), 1L);
							Assert.assertNotNull(modelField.get(validatable.queen));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			},
			{
				State.ACTIVE, 
				State.IDLE, 
				MsgStop.instance(),
				TestActorRef.create(ActorSystem.create("DroneSystem"), MockActor.makeProps(), "testDrone"),
				new Initializer() {
					public void init(UntypedActor actor) throws Exception {
					}
				},
				new Validator<QueenValidatable>() {
					public void validate(QueenValidatable validatable) {
						try {
							Assert.assertTrue(validatable.sender.underlyingActor().getLastMessage() instanceof MsgNotReady);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			},
			{
				State.ACTIVE, 
				State.ACTIVE, 
				MsgGetModel.instance(),
				TestActorRef.create(ActorSystem.create("DroneSystem"), MockActor.makeProps(), "testDrone"),
				new Initializer() {
					public void init(UntypedActor actor) throws Exception {
					}
				},
				new Validator<QueenValidatable>() {
					public void validate(QueenValidatable validatable) {
						try {
							Assert.assertTrue(validatable.sender.underlyingActor().getLastMessage() instanceof MsgModel);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			},
			{
				State.ACTIVE, 
				State.ACTIVE, 
				new MsgUpdateModel(new double[5]),
				TestActorRef.create(ActorSystem.create("DroneSystem"), MockActor.makeProps(), "testDrone"),
				new Initializer() {
					public void init(UntypedActor actor) throws Exception {
					}
				},
				new Validator<QueenValidatable>() {
					public void validate(QueenValidatable validatable) {
						try {
							Assert.assertTrue(validatable.sender.underlyingActor().getLastMessage() instanceof MsgUpdateDone);
							Assert.assertTrue(validatable.modelUpdater.updated);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		};
	}
}
