package twizansk.hivemind.common;

import java.util.HashMap;
import java.util.Map;

import akka.actor.UntypedActor;

public abstract class StateMachine extends UntypedActor {

	protected Object state;
	private Map<Object, Map<Object, Transition<?>>> transitions = new HashMap<Object, Map<Object,Transition<?>>>();
	private Map<Object, Transition<?>> commonTransitions = new HashMap<Object, StateMachine.Transition<?>>();

	/**
	 * A transition from one state to another, triggered by a message. The
	 * transition involves the mutation of the state and the execution of a
	 * predefined action. The transition can also include a condition. If the
	 * condition fails, the transition is aborted.
	 * 
	 * @author Tommer Wizansky
	 * 
	 * @param <T>
	 */
	public static class Transition<T extends StateMachine> {

		private final Object state;
		private final Action<T> action;
		private final Condition<T> condition;
		
		
		public Transition(Object state, Action<T> action, Condition<T> condition) {
			this.state = state;
			this.action = action;
			this.condition = condition;
		}
		
		public Transition(Object state, Action<T> action) {
			this(state, action, null);
		}
		
		public Transition(Object state) {
			this(state, null, null);
		}

		@SuppressWarnings("unchecked")
		private void apply(StateMachine stateMachine, Object message) {
			if (this.condition == null || this.condition.isSatisfied((T) stateMachine, message)) {
				System.out.printf("Received: %s. Transitioning from %s to %s\n", message.getClass().getSimpleName(), stateMachine.state, this.state);
				if (this.action != null) {
					this.action.apply((T) stateMachine, message);
				}
				stateMachine.state = this.state;
			}
		}

	}

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
		if (commonTransitions.containsKey(message)) {
			return commonTransitions.get(message.getClass());
		}
		
		// Now look in the transtions specific to the given state.
		Map<Object, Transition<?>> stateTransitions = this.transitions.get(state);
		return stateTransitions == null ? null : stateTransitions.get(message.getClass());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		Transition<?> transition = this.getTransition(this.state, message);
		if (transition != null) {
			transition.apply(this, message);
		} else {
			unhandled(message);
		}
	}

}
