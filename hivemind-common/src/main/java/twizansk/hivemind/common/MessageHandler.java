package twizansk.hivemind.common;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

/**
 * Interface for handling Akka messages.
 * 
 * @author Tommer Wizansky
 *
 */
public interface MessageHandler<T> {
	
	/**
	 * Should the handler handle the given request.
	 * 
	 * @param msg
	 * @return
	 * 		True if should handle.  False otherwise.
	 */
	boolean shouldHandle(Object msg);
	
	/**
	 * Handle the message.
	 * 
	 * @param msg
	 */
	void handle(T msg, ActorContext context, ActorRef recipient, ActorRef sender);
	
	
}
