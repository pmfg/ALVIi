package pt.lsts.alvii;

import android.util.Log;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Vector;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.lsf.LsfIndex;
import pt.lsts.imc.lsf.LsfIndexListener;

/**
 * @author jqcorreia
 * @author pdias
 */

class LsfLogSource implements IMraLogGroup {

    private static final String TAG = "MEU LsfLogSource";

    /** LSF (LSTS Serialized Format) file extension */
    public static final String FILE_TYPE_LSF = "lsf";
    /** GZipped compressed LSF (LSTS Serialized Format) file extension */
    public static final String FILE_TYPE_LSF_COMPRESSED = "lsf.gz";
    /** BZipped2 compressed LSF (LSTS Serialized Format) file extension */
    public static final String FILE_TYPE_LSF_COMPRESSED_BZIP2 = "lsf.bz2";

    IMCDefinition defs;
    LsfIndex index;
    File lsfFile;
    LsfIndexListener listener = null;
    String[] existingMessages = null;
    Collection<Integer> vehicleSources;


    public LsfLogSource(String filename, LsfIndexListener listener) throws Exception {
        this(new File(filename), listener);
    }

    public LsfLogSource(File file, LsfIndexListener listener) throws Exception {
        this.listener = listener;
        loadLog(file);
    }

    public void cleanup() {
        if (index != null)
            index.cleanup();
    }

    private void loadLog(File f) throws Exception {
        if(!f.canRead())
            throw(new IOException());

        if (f.getName().toLowerCase().endsWith(FILE_TYPE_LSF_COMPRESSED)) {
            GzipCompressorInputStream mmgis = new GzipCompressorInputStream(new FileInputStream(f), true);
            File outFile = new File(f.getAbsolutePath().replaceAll("\\.gz$", ""));
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            try {
                StreamUtil.copyStreamToFile(mmgis, outFile);
                f = outFile;
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
            finally {
                try {
                    mmgis.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (f.getName().toLowerCase().endsWith(FILE_TYPE_LSF_COMPRESSED_BZIP2)) {
            BZip2CompressorInputStream mmgis = new BZip2CompressorInputStream(new FileInputStream(f), true);
            File outFile = new File(f.getAbsolutePath().replaceAll("\\.bz2$", ""));
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            try {
                StreamUtil.copyStreamToFile(mmgis, outFile);
                f = outFile;
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
            finally {
                try {
                    mmgis.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        lsfFile = f;
        File defsFile1 = new File(f.getParent()+"/IMC.xml");
        File defsFile2 = new File(f.getParent()+"/IMC.xml.gz");
        if(defsFile1.canRead()) {
            defs = new IMCDefinition(new FileInputStream(defsFile1));
        }
        else if (defsFile2.canRead()) {
            defs = new IMCDefinition(new GzipCompressorInputStream(new FileInputStream(defsFile2), true));
        }
        else {
            defs = IMCDefinition.getInstance(); // If IMC.xml isn't present use the default ones
        }
        index = new LsfIndex(lsfFile, defs, listener);
    }

    @Override
    public String name() {
        return lsfFile.getParentFile().getName();
    }

    @Override
    public LinkedHashMap<String, Object> metaInfo() {
        return null;
    }

    /**
     * @return The IMraLog object related to the type of message stated in logName
     */
    @Override
    public IMraLog getLog(String logName) {

        if (index == null)
            return null;

        try {
            int i = index.getFirstMessageOfType(logName);
            if(i != -1)
                return new LsfMraLog(index, logName);
            else
                return null;
        }
        catch (Exception e) {
            Log.i(TAG, "<###>Index is: " + index);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean parse(URI uri) {
        return false;
    }



    @Override
    public String[] listLogs() {

        if (existingMessages == null) {
            Vector<String> list = new Vector<String>();
            Vector<Integer> indexes = new Vector<Integer>();

            for(int i = 0; i < index.getNumberOfMessages(); i++) {
                int type = index.typeOf(i);
                if (!indexes.contains(type)) {
                    indexes.add(type);
                    String msgName = index.getDefinitions().getMessageName(type);
                    if (msgName == null) {
                        System.err.println("Message type not found in the definitions: "+type+", "+(index.getNumberOfMessages()-i));
                    }
                    else
                        list.add(index.getDefinitions().getMessageName(type));
                }
            }
            existingMessages = list.toArray(new String[list.size()]);
        }
        return existingMessages;
    }


    @Override
    public File getDir() {
        if (lsfFile != null) {
            return lsfFile.getParentFile();
        }
        return null;
    }

    @Override
    public File getFile(String name) {
        if (lsfFile != null) {
            File f = new File(lsfFile.getParentFile(), name);
            if(f.canRead())
                return f;
            else
                return null;
        }
        return null;
    }

    @Override
    public String getEntityName(int src, int src_ent) {
        return index.getEntityName(src, src_ent);
    }

    @Override
    public String getSystemName(int src) {
        return index.getSystemName(src);
    }

    public Collection<Integer> getMessageGenerators(String msgType) {
        Vector<Integer> sources = new Vector<Integer>();
        int idx = 0;
        while((idx = index.getNextMessageOfType(msgType,idx))!=-1) {
            if(!sources.contains(index.sourceOf(idx))) {
                sources.add(index.sourceOf(idx));
            }
        }
        return sources;
    }

    public Collection<Integer> getVehicleSources() {
        // Actually only vehicles generate EntityInfo messages so it is a good way to
        // differentiate between vehicles and other nodes.
        if(vehicleSources == null)
            vehicleSources = getMessageGenerators("EntityInfo"); // Just generate this collection once

        // Filter out Id's over 0x4000 (CCU id range start value)
        ArrayList<Integer> toRemove = new ArrayList<Integer>();
        for(Integer v: vehicleSources) {
            if(v > 0x4000) {
                toRemove.add(v);
            }
        }

        vehicleSources.removeAll(toRemove);
        return vehicleSources;
    }

    @Override
    public LsfIndex getLsfIndex() {
        return index;
    }
}
