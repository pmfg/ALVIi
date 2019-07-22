package pt.lsts.accl.event;


import pt.lsts.accl.sys.Sys;


/**
 *
 * Event generated when user or application selects a System as Main/Active
 */
public class EventMainSystemSelected extends AbstractACCLEvent {

	private Sys sys;

	/**
	 *
	 * Simple constructor
	 * @param sys The system that originated this event
	 */
	public EventMainSystemSelected(Sys sys) {
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
		return super.toString() + " - "+sys.getName()+" selected as Main system";
	}

}
