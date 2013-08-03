package twizansk.hivemind.integration;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import twizansk.hivemind.messages.drone.UpdateModel;
import twizansk.hivemind.messages.external.Start;
import twizansk.hivemind.messages.external.Stop;
import twizansk.hivemind.messages.queen.NotReady;
import twizansk.hivemind.messages.queen.Ready;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;

public class HiveMindIntegrationTest {
	
	private final static Timeout timeout = new Timeout(Duration.create(5, "minutes"));
	private ActorRef queen;
	private ActorRef drone;
	
	public static class Initializer extends UntypedActor {

		private final static String queenPath = "akka.tcp://QueenSystem@127.0.0.1:2552/user/queen";
		private final static String dronePath = "akka.tcp://DroneSystem@127.0.0.1:2553/user/drone";
		private volatile ActorRef queenRef;
		private volatile ActorRef droneRef;
		
		public Initializer() {
			ActorSelection queenSelection = getContext().actorSelection(queenPath);
			ActorSelection droneSelection = getContext().actorSelection(dronePath);
			queenSelection.tell(new Identify(queenPath), getSelf());
			droneSelection.tell(new Identify(dronePath), getSelf());
		}
		
		@Override
		public void onReceive(Object msg) throws Exception {
			if (msg instanceof ActorIdentity) {
				ActorRef ref = ((ActorIdentity) msg).getRef();
				if (ref.path().toString().equals(queenPath)) {
					queenRef = ref;
				} else if (ref.path().toString().equals(dronePath)) {
					droneRef = ref;
				}
			} else if (queenRef == null || droneRef == null) {
				Thread.sleep(100);
				getSelf().tell(msg, getSender());
			} else if (msg.equals("GetQueen")) {
				getSender().tell(queenRef, getSelf());
			} else if (msg.equals("GetDrone")) {
				getSender().tell(droneRef, getSelf());
			} else {
				unhandled(msg);
			}
		}
	}
	
	@BeforeClass
	public void init() throws Exception {
		final ActorSystem system = ActorSystem.create("MySystem");
		ActorRef initializer = system.actorOf(Props.create(Initializer.class), "initializer");
		Future<Object> queenFuture = Patterns.ask(initializer, "GetQueen", timeout);
		Future<Object> droneFuture = Patterns.ask(initializer, "GetDrone", timeout);
		this.queen = (ActorRef) Await.result(queenFuture, timeout.duration());
		this.drone = (ActorRef) Await.result(droneFuture, timeout.duration());
	}
	
	/**
	 * Check that the queen can be started and stopped externally.
	 */
	@Test
	public void startStopQueen() throws Exception {
		// Start the queen
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
		f = Patterns.ask(queen, new UpdateModel(), timeout);
		Object response = Await.result(f, timeout.duration());
		Assert.assertTrue(response instanceof NotReady);
	}
	
}
