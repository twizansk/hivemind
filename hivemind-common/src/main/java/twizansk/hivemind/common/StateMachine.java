package twizansk.hivemind.common;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorIdentity;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.remote.RemotingLifecycleEvent;

public abstract class StateMachine extends UntypedActor {

	protected final LoggingAdapter log = Logging.getLogger(getContext().system(), StateMachine.class);
	
	protected Object state;
	private Map<Object, Map<Object, Transition<?>>> transitions = new HashMap<Object, Map<Object,Transition<?>>>();
	private Map<Object, Transition<?>> commonTransitions = new HashMap<Object, Transition<?>>();
	private Map<String, RemoteActor> remoteActors = new HashMap<>();

	/**
	 * An action to be performed on a state machine given a message.
	 * 
	 * @author Tommer Wizansky
	 * 
	 * @param <T>
	 */
	public static interface Action<T extends StateMachine> {

		void apply(T actor, Object message);

	}

	/**
	 * Represents a condition of the completion of a transition. Only if
	 * isSatisfied returns true, is the transition performed.
	 * 
	 * @author Tommer Wizansky
	 * 
	 * @param <T>
	 */
	public static interface Condition<T> {

		boolean isSatisfied(T actor, Object message);

	}

	public StateMachine() {
		this.getContext().system().eventStream().subscribe(this.getSelf(), RemotingLifecycleEvent.class);
	}
	
	/**
	 * Add a transition to the state machine
	 * 
	 * @param state
	 *            The original state of the transition
	 * @param message
	 *            The message that triggers the transition.
	 * @param transition
	 *            The transition
	 */
	protected void addTransition(Object state, Object message, Transition<?> transition) {
		Map<Object, Transition<?>> stateTransitions = this.transitions.get(state);
		if (stateTransitions == null) {
			stateTransitions = new HashMap<Object, Transition<?>>();
			transitions.put(state, stateTransitions);
		}
		stateTransitions.put(message, transition);
		transition.setLog(log);
	}
	
	/**
	 * Add a transition from any state, triggered by a given message.
	 * 
	 * @param message
	 * 		The trigger message
	 * @param transition
	 * 		The transtion
	 */
	protected void addTransition(Object message, Transition<?> transition) {
		commonTransitions.put(message, transition);
		transition.setLog(log);
	}

	/**
	 * Get the transition from a given state, triggered by the specified message
	 * 
	 * @param state
	 *            The original state.
	 * @param message
	 *            The message.
	 * @return The transition to the next state.
	 */
	protected Transition<?> getTransition(Object state, Object message) {
		// Look for the transition in the common transitions first.
		Transition<?> transition = commonTransitions.get(message.getClass());
		if (transition != null && transition.checkCondition(this, message)) {
			return commonTransitions.get(message.getClass());
		}
		
		// Now look in the transtions specific to the given state.
		Map<Object, Transition<?>> stateTransitions = this.transitions.get(state);
		transition = stateTransitions == null ? null : stateTransitions.get(message.getClass());
		return transition == null || !transition.checkCondition(this, message) ? null : transition;
	}
	
	/**
	 * Register a remote actor with the state machine.  A remote actro will be monitored for lifecycle events and 
	 * will reconnect in event of termination.
	 * 
	 * @param path
	 * 		The remote path.
	 * @return
	 * 		A reference to the {@link RemoteActor} object.
	 */
	protected RemoteActor registerRemoteActor(String path) {
		RemoteActor remoteActor = new RemoteActor(getSelf(), path, getContext());
		remoteActors.put(path, remoteActor);
		return remoteActor;
	}
	
	/**
	 * When a terminated event is received, set the actor's state to terminated and lookup it up again.
	 * 
	 * @param event
	 */
	private void onTerminated(Terminated event) {
		RemoteActor remoteActor = remoteActors.get(event.actor().path().toString());
		if (remoteActor != null) {
			remoteActor.terminated();
			remoteActor.lookup();
		}
	}
	
	/**
	 * When an {@link ActorIdentity} message is received, initialize the associated remote actor.
	 * 
	 * @param identity
	 */
	private void onIdentity(ActorIdentity identity) {
		RemoteActor remoteActor = remoteActors.get(identity.getRef().path().toString());
		if (remoteActor != null) {
			remoteActor.setRef(identity.getRef());
			this.getContext().watch(identity.getRef());
		}
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof Terminated) {
			// Handle remote actor termination
			this.onTerminated((Terminated) message);
		} else if (message instanceof ActorIdentity) {
			// Handle remote actor reconnection
			this.onIdentity((ActorIdentity) message);
		} 

		// Handle all other events.
		Transition<?> transition = this.getTransition(this.state, message);
		if (transition != null) {
			transition.apply(this, message);
		} else {
			unhandled(message);
		}
		
	}

}
