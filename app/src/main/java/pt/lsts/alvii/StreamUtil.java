package pt.lsts.alvii;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Paulo Dias
 *
 */

class StreamUtil {
    /** To avoid instantiation */
    private static final String TAG = "MEU StreamUtil";

    private StreamUtil() {
        super();
    }

    /**
     * Copies an input stream to the output stream until end of stream.
     * This won't close the streams!! You have to close them.
     *
     * @param inStream
     * @param outStream
     * @return
     */
    public static boolean copyStreamToStream (InputStream inStream, OutputStream outStream) {
        try {
            byte[] extra = new byte[50000];

            int ret = 0;
            int pos = 0;

            for (;;) {
                ret = inStream.read(extra);
                //Log.i(TAG, "copyStreamToStream> ret: " + ret);
                if (ret != -1) {
                    byte[] extra1 = new byte[ret];
                    System.arraycopy (extra, 0 , extra1, 0 , ret);
                    outStream.write(extra1);
                    outStream.flush();
                    pos =+ret;
                    //Log.i(TAG, "copyStreamToStream> pos: " + pos);
                }
                else {
                    //Log.i(TAG, "copyStreamToStream> end <");
                    break;
                }
            }
            return true;
        }
        catch (IOException e) {
            Log.i(TAG, "copyStreamToStream", e);
            return false;
        }
    }


    /**
     * Copies an input stream to a string until end of stream.
     * This won't close the stream!! You have to close them.
     *
     * @param inStream
     * @return
     */
    public static String copyStreamToString (InputStream inStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copyStreamToStream(inStream, baos);
        String ret = baos.toString();
        return ret;
    }

    /**
     * Copies an input stream to a file until end of stream.
     * This won't close the stream!! You have to close them.
     * This will overwrite the file.
     *
     * @param inStream
     * @param outFile
     * @return
     */
    public static boolean copyStreamToFile (InputStream inStream, File outFile) {
        return copyStreamToFile(inStream, outFile, false);
    }


    /**
     * Copies an input stream to a file until end of stream.
     * This won't close the stream!! You have to close them.
     *
     * @param inStream
     * @param outFile
     * @param append
     * @return
     */
    public static boolean copyStreamToFile (InputStream inStream, File outFile, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile, append);
            boolean ret = copyStreamToStream(inStream, fos);
            return ret;
        }
        catch (Exception e) {
            Log.i(TAG, "copyStreamToFile", e);
            return false;
        }
        finally {
            try {
                if (fos != null)
                    fos.close();
            }
            catch (Exception e) {
                Log.i(TAG, "copyStreamToFile", e);
            }
        }
    }

    /**
     * Copies an input stream to a temporary file until end of stream.
     * This won't close the stream!! You have to close them.
     *
     * @param inStream
     * @return
     */
    public static File copyStreamToTempFile (InputStream inStream) {
        File fx;
        File storageDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "alvii" + File.separator + "log" + File.separator + "tmp");
        if (!storageDir.exists()) {
            Log.i(TAG, "Creating foler");
            boolean success = storageDir.mkdirs();
            if (success)
                Log.i(TAG, "Folder created");
            else {
                Log.i(TAG, "Error creating folder");;
            }
        }
        else {
            try {
                File tmpDir = storageDir;
                fx = File.createTempFile("neptus_", "tmp", tmpDir);
                fx.deleteOnExit();
                boolean ret = copyStreamToFile(inStream, fx);
                if (ret)
                    return fx;
                else
                    return null;
            } catch (IOException e) {
                Log.i(TAG, "copyStreamToTempFile", e);
                return null;
            }
        }

        return null;
    }

    /**
     * Works similarly to {@link InputStream} but ensures that len is read, or returns -1 if EOS.
     *
     * @see {@link InputStream}
     *
     * @param in the InputStream to read from.
     * @param b the buffer into which the data is read.
     * @param off the start offset in array <code>b</code> at which the data is written.
     * @param len The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer, or <code>-1</code> if there is no more data because the
     *         end of the stream has been reached.
     * @throws IOException
     */
    public static int ensureRead (InputStream in, byte[] b, int off, int len) throws IOException {
        int actualRead = 0;
        int ret = in.read(b, off, len);
        if (ret == -1) {
            //throw new IOException("Return -1");
            return ret;
        }
        actualRead += ret;
        while (actualRead != len) {
            int left = len - actualRead;
            ret = in.read(b, off+actualRead, left);
            if (ret == -1) {
                //throw new IOException("Return -1");
                return ret;
            }
            actualRead += ret;
        }
        return actualRead;
    }
}
