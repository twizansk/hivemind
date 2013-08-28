package twizansk.hivemind.test;

/**
 * Interface for validator objects. These are used by test to run arbitrary sets
 * of validations on validatable object of a pre defined type.
 * 
 * @author Tommer Wizansky
 * 
 * @param <T>
 */
public interface Validator<T extends Validatable> {
	void validate(T validatable);
}