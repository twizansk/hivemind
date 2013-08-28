package twizansk.hivemind.queen;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.api.objective.ModelFactory;
import twizansk.hivemind.common.Model;
import twizansk.hivemind.drone.Drone;
import twizansk.hivemind.messages.drone.MsgUpdateModel;
import twizansk.hivemind.messages.external.MsgConnectAndStart;
import twizansk.hivemind.messages.queen.MsgNotReady;
import twizansk.hivemind.messages.queen.MsgUpdateDone;
import twizansk.hivemind.queen.Queen.State;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.TestActorRef;

@Test(singleThreaded=true)
public class QueenTest {

	class MockModelUpdater extends ModelUpdater {
		private boolean updated = false;

		@Override
		public void update(MsgUpdateModel updateModel, Model model, long t) {
			super.update(updateModel, model, t);
			this.updated = true;
		}
	}
	
	class MockModel implements Model {
		private static final long serialVersionUID = 1L;
	}

	private static Field stateField;
	
	@BeforeClass
	public void init() throws NoSuchFieldException, SecurityException {
		 stateField = Queen.class.getDeclaredField("state");
		 stateField.setAccessible(true);
	}
	
	public void startStop() throws Exception {
		ActorSystem system = ActorSystem.create("QueenSystem");
		try {
			Props props = Queen.makeProps(new ModelFactory() {
				public Model createModel() {
					return new MockModel();
				}
			}, null);
			TestActorRef<Queen> ref = TestActorRef.create(system, props, "testQueen");
			Await.result(Patterns.ask(ref, MsgConnectAndStart.instance(), 3000), Duration.create(1, TimeUnit.SECONDS));
			Queen queen = ref.underlyingActor();
			State state = (State) stateField.get(queen);
			Assert.assertEquals(state, State.ACTIVE);
		} finally {
			system.shutdown();
		}
	}
	
	@Test
	public void updateModel() throws Exception {
		ActorSystem system = ActorSystem.create("QueenSystem");
		try {
			MockModelUpdater modelUpdater = new MockModelUpdater();
			Props props = Queen.makeProps(new ModelFactory() {
				public Model createModel() {
					return new MockModel();
				}
			}, modelUpdater);
			TestActorRef<Queen> ref = TestActorRef.create(system, props, "testQueen");
			Await.result(Patterns.ask(ref, MsgConnectAndStart.instance(), 3000), Duration.create(1, TimeUnit.SECONDS));
			Future<Object> future = Patterns.ask(ref, new MsgUpdateModel(null), 3000);
			MsgUpdateDone updateDone = (MsgUpdateDone) Await.result(future, Duration.create(1000, "seconds"));
			Assert.assertNotNull(updateDone);
			Assert.assertTrue(future.isCompleted());
			Assert.assertTrue(modelUpdater.updated);
		} finally {
			system.shutdown();
		}
	}
	
	@Test
	public void updateModelNotReady() throws Exception {
		ActorSystem system = ActorSystem.create("QueenSystem");
		try {
			MockModelUpdater modelUpdater = new MockModelUpdater();
			Props props = Queen.makeProps(new ModelFactory() {
				public Model createModel() {
					return new MockModel();
				}
			}, modelUpdater);
			TestActorRef<Drone> ref = TestActorRef.create(system, props, "testQueen");
			Future<Object> future = Patterns.ask(ref, new MsgUpdateModel(null), 3000);
			MsgNotReady notReady = (MsgNotReady) Await.result(future, Duration.create(1000, "seconds"));
			Assert.assertNotNull(notReady);
		} finally {
			system.shutdown();
		}
	}
}
