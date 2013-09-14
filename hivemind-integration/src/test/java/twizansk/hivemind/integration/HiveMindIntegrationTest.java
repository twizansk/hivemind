package twizansk.hivemind.integration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.api.model.Model;
import twizansk.hivemind.api.model.MsgUpdateModel;
import twizansk.hivemind.common.SynchronousActorLookup;
import twizansk.hivemind.messages.drone.MsgGetModel;
import twizansk.hivemind.messages.external.MsgConnectAndStart;
import twizansk.hivemind.messages.external.MsgReset;
import twizansk.hivemind.messages.external.MsgStop;
import twizansk.hivemind.messages.queen.MsgNotReady;
import twizansk.hivemind.messages.queen.MsgReady;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;

@Test(singleThreaded=true)
public class HiveMindIntegrationTest {
	
	private final static String queenPath = "akka.tcp://QueenSystem@127.0.0.1:2552/user/queen";
	private final static String dronePath = "akka.tcp://DroneSystem@127.0.0.1:2553/user/drone";
	
	private final static Timeout timeout = new Timeout(Duration.create(5, "seconds"));
	private ActorRef queen;
	private ActorRef drone;
	private ActorSystem system;
	
	@BeforeClass
	public void init() throws Exception {
		system = ActorSystem.create("MySystem");
		
		// Get the queen
		ActorRef lookup = system.actorOf(SynchronousActorLookup.makeProps(queenPath));
		Future<Object > f = Patterns.ask(lookup, SynchronousActorLookup.GET_ACTOR_REF, Timeout.longToTimeout(100000));
		queen = (ActorRef) Await.result(f, Duration.Inf());
		
		lookup = system.actorOf(SynchronousActorLookup.makeProps(dronePath));
		f = Patterns.ask(lookup, SynchronousActorLookup.GET_ACTOR_REF, Timeout.longToTimeout(100000));
		drone = (ActorRef) Await.result(f, Duration.Inf());
	}
	
	@BeforeMethod
	public void stopActors() {
		drone.tell(MsgStop.instance(), null);
 		queen.tell(MsgStop.instance(), null);
 		drone.tell(MsgReset.instance(), null);
	}
	
	/**
	 * Check that the queen can be started and stopped externally.
	 */
	@Test
	public void startStopQueen() throws Exception {
		Future<Object> f = Patterns.ask(queen, MsgConnectAndStart.instance(), timeout);
		Object response = Await.result(f, timeout.duration());
		Assert.assertTrue(response instanceof MsgReady);
		
		// Stop the queen
		f = Patterns.ask(queen, MsgStop.instance(), timeout);
		response = Await.result(f, timeout.duration());
		Assert.assertTrue(response instanceof MsgNotReady);
	}
	
	/**
	 * When the queen is in the idle state, check that update model messages result in a NotReady response. 
	 */
	@Test
	public void updateModelWhenIdle() throws Exception {
		Future<Object> f = Patterns.ask(queen, MsgStop.instance(), timeout);
		Await.result(f, timeout.duration());
		f = Patterns.ask(queen, new MsgUpdateModel(null), timeout);
		Object response = Await.result(f, timeout.duration());
		Assert.assertTrue(response instanceof MsgNotReady);
	}
	
	/**
	 * Simulate the full startup flow as well as an initial update.
	 * @throws Exception 
	 */
	@Test
	public void startupAndUpdate() throws Exception {
		queen.tell(MsgConnectAndStart.instance(), null);
		drone.tell(MsgConnectAndStart.instance(), null);
		Thread.sleep(500000);
		Future<Object> f = Patterns.ask(queen, MsgGetModel.instance(), 1000);
		Object model = Await.result(f, Duration.create(1, TimeUnit.SECONDS));
 		Assert.assertTrue(Arrays.equals(((Model) model).params, new double[] {13, 13, 13}));
 		
 		// restart the queen.  check that the model is reset.
 		drone.tell(MsgStop.instance(), null);
 		queen.tell(MsgStop.instance(), null);
 		queen.tell(MsgConnectAndStart.instance(), null);
 		f = Patterns.ask(queen, MsgGetModel.instance(), 1000);
		model = Await.result(f, Duration.create(1, TimeUnit.SECONDS));
 		Assert.assertTrue(Arrays.equals(((Model) model).params, new double[] {0, 0, 0}));
	}
	
}
