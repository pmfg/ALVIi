package pt.lsts.alvii;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Decompresses bytes read from a input stream of data
 * Monitors input data stream size
 *
 * @author pdias
 */
abstract class FilterCopyDataMonitor extends FilterInputStream {

    public long downloadedSize = 0;
    /**
     * @param in
     */
    public FilterCopyDataMonitor(InputStream in) {
        super(in);
        downloadedSize = 0;
    }
    @Override
    public int read() throws IOException {
        int tmp = super.read();
        downloadedSize += (tmp == -1) ? 0 : 1;
        if (tmp != -1)
            updateValueInMessagePanel();
        return tmp;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int tmp = super.read(b, off, len);
        downloadedSize += (tmp == -1) ? 0 : tmp;
        if (tmp != -1)
            updateValueInMessagePanel();
        return tmp;
    }
    public abstract void updateValueInMessagePanel();
}
