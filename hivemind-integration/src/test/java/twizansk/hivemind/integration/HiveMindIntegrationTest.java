package twizansk.hivemind.integration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.common.ActorLookup;
import twizansk.hivemind.messages.drone.GetModel;
import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.external.Start;
import twizansk.hivemind.messages.external.Stop;
import twizansk.hivemind.messages.queen.NotReady;
import twizansk.hivemind.messages.queen.Ready;
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
		ActorRef lookup = system.actorOf(ActorLookup.makeProps(queenPath));
		Future<Object > f = Patterns.ask(lookup, ActorLookup.GET_ACTOR_REF, Timeout.longToTimeout(100000));
		queen = (ActorRef) Await.result(f, Duration.Inf());
		
		lookup = system.actorOf(ActorLookup.makeProps(dronePath));
		f = Patterns.ask(lookup, ActorLookup.GET_ACTOR_REF, Timeout.longToTimeout(100000));
		drone = (ActorRef) Await.result(f, Duration.Inf());
	}
	
	/**
	 * Check that the queen can be started and stopped externally.
	 */
	@Test
	public void startStopQueen() throws Exception {
		Future<Object> f = Patterns.ask(queen, Start.instance(), timeout);
		Object response = Await.result(f, timeout.duration());
		Assert.assertTrue(response instanceof Ready);
		
		// Stop the queen
		f = Patterns.ask(queen, Stop.instance(), timeout);
		response = Await.result(f, timeout.duration());
		Assert.assertTrue(response instanceof NotReady);
	}
	
	/**
	 * When the queen is in the idle state, check that update model messages result in a NotReady response. 
	 */
	@Test
	public void updateModelWhenIdle() throws Exception {
		Future<Object> f = Patterns.ask(queen, Stop.instance(), timeout);
		Await.result(f, timeout.duration());
		f = Patterns.ask(queen, new UpdateModel(null), timeout);
		Object response = Await.result(f, timeout.duration());
		Assert.assertTrue(response instanceof NotReady);
	}
	
	/**
	 * Simulate the full startup flow as well as an initial update.
	 * @throws Exception 
	 */
	@Test
	public void startupAndUpdate() throws Exception {
		queen.tell(Start.instance(), null);
		drone.tell(Start.instance(), null);
		Thread.sleep(5000);
		Future<Object> f = Patterns.ask(queen, GetModel.instance(), 1000);
		Object model = Await.result(f, Duration.create(1, TimeUnit.SECONDS));
		System.out.println(Arrays.toString(((IntegrationModel) model).params));
 		Assert.assertTrue(Arrays.equals(((IntegrationModel) model).params, new double[] {13, 13, 13}));
	}
	
}
