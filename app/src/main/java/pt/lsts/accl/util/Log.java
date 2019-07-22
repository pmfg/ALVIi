package pt.lsts.accl.util;


import pt.lsts.accl.bus.AcclBus;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.IMCOutputStream;
import pt.lsts.imc.ImcStringDefs;
import pt.lsts.imc.net.IMessageLogger;

import java.io.File;
import java.util.Vector;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;


/**
 *
 * Class Responsible for logging the IMCMessages.
 * Based on {@link pt.lsts.imc.lsf.LsfMessageLogger}
 *
 * Singleton. Only public method needed is {@link #log(IMCMessage)}
 *
 */
public class Log{

    private static Log instance = null;
    private IMCOutputStream ios = null;
    protected SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd'/'HHmmss");
    {
    	fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    protected String logPath = null;
    
    public String logBaseDir = "/storage/emulated/0/data/pt.lsts.accl/log/messages/";//versions 4.2 and above
    public static String logBaseDirLegacy = "/storage/sdcard0/data/pt.lsts.accl/log/messages/";//versions 4.0 and 4.1
    public static boolean legacyBool = false;// if true, path will be logBaseDirLegacy

    /**
     * Private constructor to ensure Singleton Pattern.
     */
    private Log() {
        changeLog();

        try {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (Log.this) {
                            IMCOutputStream copy = ios;
                            ios = null;
                            copy.close();
                            AcclBus.post("WARN - "+"Closed Log");
                        }                                               
                    }
                    catch (Exception e) {
                        AcclBus.post("ERROR - "+e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
            AcclBus.post("ERROR - "+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the Path to the Log directory.
     * @return Full Path to the Log directory.
     */
    public String getLogDir() {
        return logPath;
    }

    /**
     * Get the Path to the Log directory of the Singleton Obj.
     * @return Full Path to the Log directory of the Singleton Obj.
     */
    public static String getLogDirSingleton() {
        return getInstance().logPath;
    }

    /**
     * Call ChangeLog: create new directory for a new log file.
     * @return true if succeed, false otherwise.
     */
    public static boolean changeLogSingleton() {
        return getInstance().changeLog();
    }

    /**
     * Reset log directory. Create a new log directory
     * @return true if succeed, false otherwise.
     */
    public boolean changeLog() {
        if (legacyBool==true)
            logPath = logBaseDirLegacy + fmt.format(new Date());
        else
            logPath = logBaseDir + fmt.format(new Date());
        
        File outputDir = new File(logPath);
        outputDir.mkdirs();
        
        IMCOutputStream iosTmp = null;
        try {
        	OutputStream fos = new GZIPOutputStream(new FileOutputStream(new File(outputDir.getAbsolutePath() + "/IMC.xml.gz")));
        	fos.write(ImcStringDefs.getDefinitions().getBytes());
        	fos.close();
        	iosTmp = new IMCOutputStream(new GZIPOutputStream(new FileOutputStream(new File(outputDir, "Data.lsf.gz"))));
        }
        catch (Exception e) {
            AcclBus.post("ERROR - "+e.getMessage());
            e.printStackTrace();
        }

        if (iosTmp != null) {
            synchronized (this) {
                if (ios != null) {
                    try {
                        ios.close();
                    }
                    catch (IOException e) {
                        AcclBus.post("ERROR - "+"IOException - "+e.getMessage());
                        e.printStackTrace();
                    }
                }
                ios = iosTmp;    
            }
            AcclBus.post("WARN - "+"Changed Log to new folder: "+logPath);
            return true;
        }
        return false;
    }

    /**
     * Get the Log Instance of Singleton Obj. Create it if it is null.
     * Private to ensure only certain methods may be called.
     * @return the Singleton Obj for Log.
     */
    public static Log getInstance() {
        if (instance == null)
            instance = new Log();

        return instance;
    }

    /**
     * Private method to Log an IMCMessage.
     * @param msg The IMCMessage to be logged.
     * @return true if succeed, false otherwise.
     */
    private synchronized boolean logMessage(IMCMessage msg) {
        try {
            if (ios != null)
                ios.writeMessage(msg);
        }
        catch (Exception e) {
            AcclBus.post("ERROR - "+e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Public method used to log an IMCMessage.
     * Main Method used outside of this class.
     * @param msg
     * @return
     */
    public static boolean log(IMCMessage msg) {
        AcclBus.post("VERBOSE - "+"Logged "+msg.getAbbrev()+":\n"+msg.toString());
        return getInstance().logMessage(msg);
    }

    /**
     *
     * Close the stream of file. Close the Log file to avoid corrupiton and missuse by Neptus MRA.
     *
     * @return true if succeed, false otherwise.
     */
    public synchronized boolean closeStream() {
        try {
            if (ios != null) {
                ios.close();                            
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     *
     * Method used to close logs to avoid errors in file.
     * Prevent erros in Neptus MRA when opening "unclosed" log files.
     *
     * @return true if succeeds, false otherwise.
     */
    public static boolean close() {
        Log ins = getInstance();
        instance = null;
        return ins.closeStream();
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
		while(true) {
			Thread.sleep(100);
			Log.log(new EstimatedState());
		}
	}

    /**
     * Change the current Path to be used to store log file.
     * @param newPath The new Full path to store Log.
     */
    public static void changeLogBaseDir(String newPath){
        getInstance().logBaseDir = newPath;
    }

    /**
     *
     * For version prior to 4.2:
     * This method should be called on Creation of first {@link android.app.Activity} and/or {@link android.app.Application}
     *
     */
    public static void activateLogBaseDirLegacy(){
        AcclBus.post("INFO - "+"ActivateLogBaseDirLegacy()");
        Log.getInstance().legacyBool=true;
    }

    /**
     *
     * For version 4.2 and above:
     * IF {@link #activateLogBaseDirLegacy()} was called, then this method should be called to revert to original path.
     *
     */
    public static void deactivateLogBaseDirLegacy(){
        AcclBus.post("INFO - "+"DeactivateLogBaseDirLegacy()");
        Log.getInstance().legacyBool=false;
    }

    public static IMessageLogger getIMessageLogger(){
        return new IMessageLogger() {
            @Override
            public void logMessage(IMCMessage message) throws Exception {
                Log.log(message);
            }
        };
    }

}