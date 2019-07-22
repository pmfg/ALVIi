package pt.lsts.accl.event;


import pt.lsts.accl.sys.Sys;


/**
 *
 * Event generated when a system hasn't send messages in 30seconds and is then considered disconnected
 */
public class EventSystemDisconnected extends AbstractACCLEvent {

	private Sys sys;

	/**
	 *
	 * Simple constructor
	 * @param sys The system that originated this event
	 */
	public EventSystemDisconnected(Sys sys) {
		this.sys = sys;
	}

	/**
	 *
	 * @return The system that originated this event
	 */
	public final Sys getSys() {
		return sys;
	}

	/**
	 *
	 * Comprehensive string to be used in logging and debugging
	 * @return Descriptive String
	 */
	@Override
	public String toString() {
		// includes time since last message
		return super.toString()+ " - "+sys.getName()+ " disconnected - last message in "+sys.getLastMsgReceivedAgeInSeconds();
	}

}
