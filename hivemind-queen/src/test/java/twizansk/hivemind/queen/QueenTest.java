package twizansk.hivemind.queen;

import org.testng.Assert;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.drone.Drone;
import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.queen.Model;
import twizansk.hivemind.messages.queen.UpdateDone;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.TestActorRef;

public class QueenTest {

	class MockModelUpdater extends ModelUpdater {
		private boolean updated = false;
		
		@Override
		public void update(UpdateModel updateModel, Model model) {
			super.update(updateModel, model);
			this.updated = true;
		}
	}
	
	@Test
	public void onReceive() throws Exception {
		ActorSystem system = ActorSystem.create("QueenSystem");
		MockModelUpdater modelUpdater = new MockModelUpdater();
		Props props = Queen.makeProps(
				new Model(), 
				modelUpdater);
		TestActorRef<Drone> ref = TestActorRef.create(system, props, "testQueen");
		Future<Object> future = Patterns.ask(ref, new UpdateModel(), 3000);
		UpdateDone updateDone = (UpdateDone) Await.result(future, Duration.create(1000, "seconds"));
		Assert.assertNotNull(updateDone);
		Assert.assertTrue(future.isCompleted());
		Assert.assertTrue(modelUpdater.updated);
	}
}
