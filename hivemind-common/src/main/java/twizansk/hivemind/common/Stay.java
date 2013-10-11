package twizansk.hivemind.common;

import twizansk.hivemind.common.StateMachine.Action;
import twizansk.hivemind.common.StateMachine.Condition;

public class Stay<T extends StateMachine> extends Transition<T> {

	public Stay(Action<T> action, Condition<T> condition) {
		super(null, action, condition);
	}
	
	public Stay(Action<T> action) {
		super(null, action, null);
	}

}
