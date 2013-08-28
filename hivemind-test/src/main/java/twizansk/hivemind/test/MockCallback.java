package twizansk.hivemind.test;

/**
 * General callback wrapper.  This can be injected into hivemind classes in many situations where mocking is desired. 
 * @author Tommer Wizansky
 *
 */
public interface MockCallback {
	void onExecute(Object... args);
//	Object getLastExecution();
}