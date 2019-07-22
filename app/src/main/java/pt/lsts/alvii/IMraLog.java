package pt.lsts.alvii;

import java.util.Collection;
import java.util.LinkedHashMap;

import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.IMCMessageType;

/**
 * This interface represents a Log File full of data that was generated / received as messages
 * @author zp
 */

class IMraLog {
    public IMCMessage getEntryAtOrAfter(long timestamp) {
        return null;
    }

    public IMCMessage getEntryAtOrAfter(long timestamp, String entityName) {
        return null;
    }

    /**
     * Retrieve a name that will identify this log file (like the name of the message)
     *
     * @return A string with the log name
     */
    public String name() {
        return null;
    }

    /**
     * Retrieve the last entry in this log
     */
    public IMCMessage getLastEntry() {
        return null;
    }

    /**
     * Retrieve the log format as a Neptus message type
     *
     * @return a MessageType indicating the various log fields.
     * The message type may or may not be attached to an existing IMC message. The latter happens for
     * new messages and also for different IMC versions.
     */
    public IMCMessageType format() {
        return null;
    }

    /**
     * Retrieve meta-information associated with this log
     *
     * @return Log Meta-information
     */
    public LinkedHashMap<String, Object> metaInfo() {
        return null;
    }

    /**
     * The timestamp of the current log entry (first entry by default)
     *
     * @return The time of the active log entry
     */
    public long currentTimeMillis() {
        return 0;
    }

    /**
     * Advance to the next Log entry and retrieve it as a IMCMessage
     *
     * @return The next log entry as a IMCMessage or null if no more message exist
     */
    public IMCMessage nextLogEntry() {
        return null;
    }

    /**
     * Goes back to the first Log Entry and retrieves it
     *
     * @return Retrieves the first log entry it an entry exists in this log file or otherwise returns null
     */
    public IMCMessage firstLogEntry() {
        return null;
    }

    /**
     * Advance millis milliseconds in the log
     *
     * @param millis The time to advance, in milliseconds
     */
    public void advance(long millis) {

    }

    /**
     * Retrieve all messages that have the given timestamp
     *
     * @param timeStampMillis Time stamp, in milliseconds
     * @return A collection of IMCMessages that have the given time stamp
     */
    public IMCMessage getCurrentEntry() {
        return null;
    }


    /**
     * Retrieve all messages that have the given timestamp
     *
     * @param timeStampMillis Time stamp, in milliseconds
     * @return A collection of IMCMessages that have the given time stamp
     */
    public Collection<IMCMessage> getExactTimeEntries(long timeStampMillis) {
        return null;
    }

    /**
     * Retrieve the total number of entries in this log
     *
     * @return the total number of entries in this log
     */
    public int getNumberOfEntries() {
        return 0;
    }
}
