package pt.lsts.alvii;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.IMCMessageType;
import pt.lsts.imc.lsf.LsfGenericIterator;
import pt.lsts.imc.lsf.LsfIndex;

public class MRALite extends AppCompatActivity {

    private static final String TAG = "MEU MRALite";
    private File storageDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "alvii");
    final Context context = this;
    int state_msg = 0;

    /** LSF (LSTS Serialized Format) file extension */
    public static final String FILE_TYPE_LSF = "lsf";
    /** GZipped compressed LSF (LSTS Serialized Format) file extension */
    public static final String FILE_TYPE_LSF_COMPRESSED = "lsf.gz";
    /** BZipped2 compressed LSF (LSTS Serialized Format) file extension */
    public static final String FILE_TYPE_LSF_COMPRESSED_BZIP2 = "lsf.bz2";
    private InputStream activeInputStream = null;

    private ProgressDialog pDialog;
    private Handler updateBarHandler;
    private Handler customHandler;
    boolean checkIndex = false;
    MraLiteStorage m_mra_storage = new MraLiteStorage(context);
    boolean alreadyConverted;
    File logLsf;
    private LsfIndex m_index;
    private String m_file_path_index;
    private boolean checkOutput = false;
    private Process process;
    private BufferedReader bufferedReader;

    //Run task periodically
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            if(checkIndex) {
                if (m_mra_storage.isFinish()) {
                    //Log.i(TAG, "Reading Log - Msg List " + m_mra_storage.getProcessStageValue() + " Total msg " + m_mra_storage.getNumberMessages());
                    //Log.i(TAG, "number of messages: " + m_mra_storage.getNumberOfListMsg());
                    updateBarHandler.removeCallbacksAndMessages(null);
                    pDialog.cancel();
                    checkIndex = false;
                    diplayList();
                }
                else {
                    updateBarHandler.post(new Runnable() {
                        public void run() {
                            if (state_msg >= 0)
                                pDialog.setMessage("Reading Log - Number Msg detected: " + m_mra_storage.getProcessStageValue() +
                                " ( " + m_mra_storage.getCntStateImcMsg() + " of " + m_mra_storage.getCntListFullImcMsg() + " )");
                        }
                    });
                }
            }

            if(checkOutput){
                try {
                    process = Runtime.getRuntime().exec("logcat -d -t 1");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                String textString = "";
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        textString = line;
                    }
                    line = "[LsfIndex]";
                    if(textString.toLowerCase().contains(line.toLowerCase())){
                        final String finalTextString = textString;
                        updateBarHandler.post(new Runnable() {
                            public void run() {
                                if (state_msg >= 0)
                                    pDialog.setMessage(finalTextString.split("]")[1].trim());
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            customHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mralite);
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
                    storageDir.toString(), ""));
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Bundle b = getIntent().getExtras();
        final String path = b.getString("BUNDLE_PATH");
        final String file_name = b.getString("BUNDLE_FILENAME");
        customHandler = new android.os.Handler();

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Reading Log...");
        pDialog.setCancelable(false);
        pDialog.show();
        updateBarHandler =new Handler();
        logLsf = new File(path + File.separator + file_name);

        File file = new File(logLsf.getParent(), "indexMessageList.stackIndex");
        if(file.exists()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Log Index");
            alertDialogBuilder
                    .setMessage("Log already Index!\n\nIndex again?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                FileUtils.forceDelete(new File(logLsf.getParent(), "indexMessageList.stackIndex"));
                            } catch (Exception e) {
                                Log.i(TAG, "Error while trying to delete mra/ folder", e);
                            }
                            startIndex(logLsf, true);
                            customHandler.postDelayed(updateTimerThread,2000);
                            checkIndex = false;
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            updateBarHandler.removeCallbacksAndMessages(null);
                            pDialog.cancel();
                            startIndex(logLsf, false);
                            m_mra_storage.getListMessgeByOldIndex(logLsf);
                            Log.i(TAG, "Reading Log - Msg " + m_mra_storage.getProcessStageValue() + " of " + m_mra_storage.getNumberMessages());
                            Log.i(TAG, "number of messages: " + m_mra_storage.getNumberOfListMsg());
                            checkIndex = false;
                            diplayList();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        else{
            customHandler.postDelayed(updateTimerThread,2000);
            checkIndex = false;
            startIndex(logLsf, true);
        }
    }

    private void startIndex(final File logLsf, final boolean fullTask){
        // Since reading log takes more time, let's run it on a separate thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                openLog(logLsf, fullTask);
            }
        }).start();
        //openLog(logLsf);
    }

    public boolean openLog(File fx, boolean fullTask) {
        File fileToOpen = null;
        updateBarHandler.post(new Runnable() {
            public void run() {
                pDialog.setMessage("Open Log (lsf) ...");
            }
        });

        if (fx.getName().toLowerCase().endsWith(FILE_TYPE_LSF_COMPRESSED)) {
            fileToOpen = extractGzip(fx);
        }
        else if (fx.getName().toLowerCase().endsWith(FILE_TYPE_LSF_COMPRESSED_BZIP2)) {
            fileToOpen = extractBzip2(fx);
        }
        else if (fx.getName().toLowerCase().endsWith(FILE_TYPE_LSF)) {
            fileToOpen = fx;
        }

        if (fileToOpen == null) {
            Log.i(TAG,"Invalid LSF file, LSF file does not exist!");
            return false;
        }
        m_file_path_index = fileToOpen.toString();
        return openLSF(fileToOpen, fullTask);
    }

    // --- Extractors ---
    /**
     * Extract GNU zip files
     * @param f input file
     * @return decompressed file
     */
    private File extractGzip(File f) {
        GzipCompressorInputStream gzDataLog = null;
        try {
            //Log.i(TAG, "Decompressing LSF Data...");
            updateBarHandler.post(new Runnable() {
                public void run() {
                    pDialog.setMessage("Decompressing LSF Data...");
                }
            });
            gzDataLog = new GzipCompressorInputStream(new FileInputStream(f), true);
            activeInputStream = gzDataLog;
            File outputFile = new File(f.getParent(), "Data.lsf");
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }

            FilterCopyDataMonitor fis = createCopyMonitor(gzDataLog);
            StreamUtil.copyStreamToFile(fis, outputFile);

            return new File(f.getParent(), "Data.lsf");
        }
        catch (Exception ioe) {
            Log.i(TAG, "Exception has been thrown Decompressing LSF Data..." + "   " + ioe);
            return null;
        }
        finally {
            if (gzDataLog != null) {
                try {
                    gzDataLog.close();
                }
                catch (IOException e) {
                    Log.i(TAG,""+e);
                }
            }
        }
    }

    private FilterCopyDataMonitor createCopyMonitor(InputStream stream) {
        FilterCopyDataMonitor fis = new FilterCopyDataMonitor(stream) {
            long targetStep = 1 * 1024 * 1024;
            long target = targetStep;
            protected String decompressed = "Decompressed" + " ";

            @Override
            public void updateValueInMessagePanel() {
                if (downloadedSize > target) {
                    target += targetStep;
                }
            }
        };
        return fis;
    }

    /**
     * Extract BZip files with BZip2 compressor.
     * @param f
     * @return decompressed file
     */
    private File extractBzip2(File f) {
        updateBarHandler.post(new Runnable() {
            public void run() {
                pDialog.setMessage("Decompressing BZip2 LSF Data...");
            }
        });
        BZip2CompressorInputStream bz2DataLog = null;
        try {
            FileInputStream fxInStream = new FileInputStream(f);
            bz2DataLog = new BZip2CompressorInputStream(fxInStream, true);
            activeInputStream = bz2DataLog;
            File outFile = new File(f.getParent(), "Data.lsf");
            if (!outFile.exists()) {
                outFile.createNewFile();
            }

            FilterCopyDataMonitor fis = createCopyMonitor(bz2DataLog);
            StreamUtil.copyStreamToFile(fis, outFile);

            return outFile;
        }
        catch (Exception e) {
            Log.i(TAG, "Exception has been thrown: " + e + "  " + "Decompressing LSF Data..." + "   " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        finally {
            if (bz2DataLog != null) {
                try {
                    bz2DataLog.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Open LSF Log file
     * @param f
     * @return
     */
    private boolean openLSF(File f, boolean fullTask) {
        if(fullTask) {
            if (!f.exists()) {
                Log.i(TAG, "Invalid LSF file - LSF file does not exist!");
                return false;
            }
            updateBarHandler.post(new Runnable() {
                public void run() {
                    pDialog.setMessage("Loading LSF Data...");
                }
            });

            File lsfDir = f.getParentFile();
            alreadyConverted = false;
            if (lsfDir.isDirectory()) {
                if (new File(lsfDir, "mra/lsf.index").canRead())
                    alreadyConverted = true;

            }
            else if (new File(lsfDir, "mra/lsf.index").canRead())
                alreadyConverted = true;

            if (alreadyConverted) {
                try {
                    FileUtils.deleteDirectory(new File(lsfDir, "mra"));
                } catch (Exception e) {
                    Log.i(TAG, "Error while trying to delete mra/ folder", e);
                }
            }

            if (!new File(lsfDir, "imc.xml").canRead()) {
                if (extractGz(lsfDir, "imc.xml"))
                    Log.i(TAG, "extract OK");
                else
                    Log.i(TAG, "extract FAIL");
            }
            try {
                openLogSource(f);
                return true;
            } catch (Exception e) {
                Log.i(TAG, "Invalid LSF index " + e.getMessage());
                return false;
            }
        }
        else{
            try {
                checkOutput = true;
                m_index = new LsfIndex(f, IMCDefinition.getInstance());
                checkOutput = false;
                checkIndex = true;
            } catch (Exception e) {
                Log.i(TAG, "" + e);
                updateBarHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.cancel();
                    }
                }, 0);
            }
        }

        return true;
    }

    private void openLogSource(final File source) {
        if (new File(source.getParent(), "imc.xml").canRead()) {
            m_mra_storage.initMraLiteStorage();
            updateBarHandler.post(new Runnable() {
                public void run() {
                    pDialog.setMessage("Indexing Log...");
                }
            });
            try {
                checkOutput = true;
                m_index = new LsfIndex(source, IMCDefinition.getInstance());
                checkOutput = false;
            } catch (Exception e) {
                Log.i(TAG, "" + e);
                updateBarHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pDialog.cancel();
                    }
                }, 0);
            }
            checkIndex = true;
            m_mra_storage.indexListOfMessage(m_index, source);
        }
        else{
            Toast.makeText(this, "no imc.xml / IMC.xml.gz found!!!", Toast.LENGTH_SHORT).show();
            updateBarHandler.removeCallbacksAndMessages(null);
            pDialog.cancel();
            checkIndex = false;
        }
    }

    private boolean extractGz(File pathGz, String nameNewFile){
        try {
            FileInputStream fis = new FileInputStream(new File(pathGz, "IMC.xml.gz"));
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(pathGz + File.separator + nameNewFile);
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            fos.close();
            gis.close();
        } catch (IOException e) {
            Log.i(TAG, "", e);
            return false;
        }
        return true;
    }

    private void diplayList() {
        final String[] listMessage = Arrays.copyOf(m_mra_storage.getListOfMessages(), m_mra_storage.getNumberOfListMsg());
        Arrays.sort(listMessage);
        ListView listView = (ListView) findViewById(R.id.msg_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listMessage);
        // Assign adapter to ListView
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                Intent intent = new Intent(MRALite.this, DisplayGraph.class);
                Bundle b = new Bundle();
                b.putString("BUNDLE_INDEX_PATH", m_file_path_index);
                b.putString("BUNDLE_IMCMESSAGE", listMessage[pos]);
                intent.putExtras(b);
                startActivity(intent);
                //Log.i(TAG, m_file_path_index );
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3)
            {
                Toast.makeText(context, "Long press to select", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
