package pt.lsts.accl.event;


import pt.lsts.accl.sys.Sys;


/**
 *
 * Event generated when an update is made available in a system
 */
public class EventSystemUpdated extends AbstractACCLEvent {

	private Sys sys;

	/**
	 *
	 * Simple constructor
	 * @param sys The system that originated this event
	 */
	public EventSystemUpdated(Sys sys) {
		this.sys = sys;
	}

	/**
	 *
	 * @return The timestamp of the event
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
		return super.toString() + " - "+sys.getName()+" updated";
	}	

}
