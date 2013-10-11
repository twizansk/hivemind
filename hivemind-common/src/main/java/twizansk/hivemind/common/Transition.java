package twizansk.hivemind.common;

import twizansk.hivemind.common.StateMachine.Action;
import twizansk.hivemind.common.StateMachine.Condition;
import akka.event.LoggingAdapter;

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
public class Transition<T extends StateMachine> {
	
	private final Object state;
	private final Action<T> action;
	private final Condition<T> condition;
	
	private LoggingAdapter log;
	
	public Transition(Object state, Action<T> action, Condition<T> condition) {
		this.state = state;
		this.action = action;
		this.condition = condition;
	}
	
	public Transition(Object state, Action<T> action) {
		this(state, action, null);
	}
	
	public Transition(Object state, Condition<T> condition) {
		this(state, null, condition);
	}
	
	public Transition(Object state) {
		this(state, null, null);
	}

	@SuppressWarnings("unchecked") 
	boolean apply(StateMachine stateMachine, Object message) {
		if (this.checkCondition(stateMachine, message)) {
			if (this.action != null) {
				this.action.apply((T) stateMachine, message);
			}
			if (log != null) {
				log.debug(String.format("message: %s, transition: %s --> %s", 
					message.getClass().getSimpleName(), stateMachine.state, this.state));
			}
			if (this.state != null) {
				stateMachine.state = this.state;
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	boolean checkCondition(StateMachine stateMachine, Object message) {
		return this.condition == null || this.condition.isSatisfied((T) stateMachine, message); 
	}
	
	void setLog(LoggingAdapter log) {
		this.log = log;
	}

}