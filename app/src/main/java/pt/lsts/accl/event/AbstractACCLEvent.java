package pt.lsts.accl.event;


import java.util.Date;


/**
 *
 * The Abstract event. All events extend this.
 */
public abstract class AbstractACCLEvent {

	/**
	 * timestamp to be used as log printout
	 */
	private double timestamp = System.currentTimeMillis() / 1000.0;

	/**
	 * Get the Age of the event
	 * @return the age of the event in seconds
	 */
	public double getAge() {
		return (System.currentTimeMillis() / 1000.0) - timestamp;
	}

	/**
	 *
	 * @return The timestamp of the event
	 */
	public double getTimestamp() {
		return timestamp;
	}

	/**
	 *
	 * Comprehensive string to be used in logging and debugging
	 * @return Descriptive String
	 */
	@Override
	public String toString() {
		return "["+new Date((long)(timestamp * 1000))+"] "+getClass().getSimpleName();
	}

}
