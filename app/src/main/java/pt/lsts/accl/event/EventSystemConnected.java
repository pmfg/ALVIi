package pt.lsts.accl.event;


import pt.lsts.accl.sys.Sys;


/**
 *
 * Event generated when a new System is connected.
 */
public class EventSystemConnected extends AbstractACCLEvent {

	private Sys sys;

	/**
	 *
	 * Simple constructor
	 * @param sys The system that originated this event
	 */
	public EventSystemConnected(Sys sys) {
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
		return super.toString()+ " - "+sys.getName()+" connected";
	}

}
