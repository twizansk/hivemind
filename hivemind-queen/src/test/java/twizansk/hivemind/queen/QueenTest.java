package twizansk.hivemind.queen;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.api.objective.IModelFactory;
import twizansk.hivemind.drone.Drone;
import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.external.Start;
import twizansk.hivemind.messages.queen.Model;
import twizansk.hivemind.messages.queen.NotReady;
import twizansk.hivemind.messages.queen.UpdateDone;
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
		public void update(UpdateModel updateModel, Model model, long t) {
			super.update(updateModel, model, t);
			this.updated = true;
		}
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
			Props props = Queen.makeProps(new IModelFactory() {
				public Model createModel() {
					return new Model();
				}
			}, null);
			TestActorRef<Queen> ref = TestActorRef.create(system, props, "testQueen");
			Await.result(Patterns.ask(ref, Start.instance(), 3000), Duration.create(1, TimeUnit.SECONDS));
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
			Props props = Queen.makeProps(new IModelFactory() {
				public Model createModel() {
					return new Model();
				}
			}, modelUpdater);
			TestActorRef<Queen> ref = TestActorRef.create(system, props, "testQueen");
			Await.result(Patterns.ask(ref, Start.instance(), 3000), Duration.create(1, TimeUnit.SECONDS));
			Future<Object> future = Patterns.ask(ref, new UpdateModel(null), 3000);
			UpdateDone updateDone = (UpdateDone) Await.result(future, Duration.create(1000, "seconds"));
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
			Props props = Queen.makeProps(new IModelFactory() {
				public Model createModel() {
					return new Model();
				}
			}, modelUpdater);
			TestActorRef<Drone> ref = TestActorRef.create(system, props, "testQueen");
			Future<Object> future = Patterns.ask(ref, new UpdateModel(null), 3000);
			NotReady notReady = (NotReady) Await.result(future, Duration.create(1000, "seconds"));
			Assert.assertNotNull(notReady);
		} finally {
			system.shutdown();
		}
	}
}
