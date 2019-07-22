package pt.lsts.alvii;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;

import pt.lsts.imc.lsf.LsfIndex;

/**
 * This interface is used to represent a logical group of log files. For instance, in a mission several log files are produced.
 * @author zp
 */

interface IMraLogGroup {
    /**
     * Retrieves the name of this log group (e.g. folder name)
     * @return The name of this log group
     */
    String name();

    /**
     * Retrieve meta-info data like mission name, description, etc
     * @return Meta-info data about this log group
     */
    LinkedHashMap<String, Object> metaInfo();

    /**
     * List all log names that exist in this group
     */
    String[] listLogs();

    /**
     * Retrieves the {@linkplain IMraLog} named logName
     */
    IMraLog getLog(String logName);

    /**
     * Tries to create a Log Group from the given URI
     * @param uri The URI for the log group (it may be a file, http, ...)
     * @return <b>true</b> if it could correctly parse the given URI or <b>false</b> if its not supported
     */
    boolean parse(URI uri);

    File getFile(String name);
    File getDir();

    void cleanup();

    String getEntityName(int src, int src_ent);
    String getSystemName(int src);

    Collection<Integer> getMessageGenerators(String msgType);
    Collection<Integer> getVehicleSources();

    LsfIndex getLsfIndex();
}
