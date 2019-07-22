package pt.lsts.alvii;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import pt.lsts.alvii.R;

/**
 * Created by Pedro Gonçalves on 7/11/17.
 * LSTS - FEUP
 */

public class FtpClient extends AppCompatActivity {
    private static final String TAG = "MEU FTP";

    private boolean spewDebug = false;
    private static final int DATA_TIMEOUT_MILLIS = 3000;
    private FTPClient ftpClient;
    String server = "10.0.2.110";
    int port = 30021;
    String user = "anonymous";
    String pass = "";
    private File storageDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "alvii");
    private Handler customHandler;
    private boolean readFtp = true;
    ListView listView_sys;
    ArrayAdapter<String> adapter;
    private String[] _name = new String[5000];
    private int counterFilesPath = 0;
    int currentIdPath = 0;
    ProgressBar pbHeaderProgress;
    boolean isLastdir = false;
    TextView exifData;
    ImageView imageDownload;
    boolean isToViewImage;
    boolean isToConnect;
    private String sysName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
                    storageDir.toString(), ""));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);

        Bundle b = getIntent().getExtras();

        String m_name = getIntent().getStringExtra("EXTRA_LOCATION_NAME");
        //Log.i("MEU", b.getString("BUNDLE_IP"));
        //Log.i("MEU", b.getString("BUNDLE_SYSTEM_PORT"));
        //Log.i("MEU", b.getString("BUNDLE_SYSTEM_NAME"));
        //Log.i("MEU", m_name);

        server = b.getString("BUNDLE_IP");
        port = Integer.parseInt(b.getString("BUNDLE_SYSTEM_PORT"));
        sysName = b.getString("BUNDLE_SYSTEM_NAME");
        //Log.i(TAG, "akkja: "+port);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);



        exifData = (TextView) findViewById(R.id.textViewExif);
        exifData.setMovementMethod(new ScrollingMovementMethod());
        exifData.setVisibility(View.INVISIBLE);

        this.setTitle("System: "+b.getString("BUNDLE_SYSTEM_NAME"));

        imageDownload = (ImageView) findViewById(R.id.imageViewDownload);
        imageDownload.setVisibility(View.INVISIBLE);

        pbHeaderProgress = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        pbHeaderProgress.setVisibility(View.VISIBLE);

        isToViewImage = false;
        isToConnect = true;

        currentIdPath = 0;

        customHandler = new android.os.Handler();
        customHandler.postDelayed(updateTimerThread, 2000);

        readFtp = true;
    }

    public void drawInterface(){
        listView_sys = (ListView) findViewById(R.id.sys_list_ftp);
        final String[] dir_name = Arrays.copyOf(_name, counterFilesPath);
        Arrays.sort(dir_name);
        pbHeaderProgress.setVisibility(View.GONE);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dir_name) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK);

                return view;
            }
        };
        listView_sys.setAdapter(adapter);
        listView_sys.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), "< " + dir_name[position] + " > Pod: " + position, Toast.LENGTH_SHORT).show();
                if (dir_name[position].toLowerCase().endsWith(".jpg"))
                {
                    isToViewImage = true;
                    //Log.i(TAG, "Download image");
                    try {
                        if(getAndSaveFile(dir_name[position], true)){
                            listView_sys.setVisibility(View.INVISIBLE);
                        }
                    } catch (IOException e) {
                        Log.i(TAG, e.toString());
                    }
                }
                else if (dir_name[position].toLowerCase().endsWith(".ini"))
                {
                    //Log.i(TAG, "Download Config.ini");
                    try {
                        getAndSaveFile(dir_name[position], false);
                        Toast.makeText(FtpClient.this, "Download Config.ini!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.i(TAG, e.toString());
                    }
                }
                else if (dir_name[position].toLowerCase().endsWith(".txt"))
                {
                    //Log.i(TAG, "Download Output.txt");
                    try {
                        getAndSaveFile(dir_name[position], false);
                        Toast.makeText(FtpClient.this, "Download Output.txt!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.i(TAG, e.toString());
                    }
                }
                else{
                    pbHeaderProgress.setVisibility(View.VISIBLE);
                    jumpToFolder(dir_name[position]);
                    readFtp = true;
                }
            }
        });
    }

    public void jumpToFolder(String folder){
        try {
            ftpClient.changeWorkingDirectory(folder);
            int code = showServerReply(ftpClient);
            if(FTPReply.isPositiveCompletion(code)) {
                currentIdPath++;
                isLastdir = false;
            }
            else {
                isLastdir = true;
            }
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }
    }

    public void getInfo() throws IOException {
        counterFilesPath = 0;

        for (FTPFile s : ftpClient.listFiles()) {
            //Log.i(TAG, s.getName());
            _name[counterFilesPath] = s.getName();
            counterFilesPath++;
        }
    }

    public boolean getAndSaveFile(String fileName, boolean isImage) throws IOException {
        boolean success;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        } else {
            success = true;
        }

        if (success)
        {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
            String formattedDate = df.format(c.getTime());
            //Log.i(TAG, formattedDate);
            String m_path = storageDir.getPath() + "/" + formattedDate + "_" + sysName + "_" + fileName;
            OutputStream buffIn = null;
            success = false;
            File file;
            try {
                if(spewDebug)
                    Log.i(TAG, m_path);
                file = new File(m_path);
                if(file.exists())
                    success = file.delete();

                buffIn = new BufferedOutputStream(new FileOutputStream(m_path));
                success = ftpClient.retrieveFile(fileName, buffIn);
            } finally {
                if (buffIn != null) {
                    buffIn.close();
                }
            }

            if(spewDebug) {
                if (success)
                    Log.i(TAG, "Save OK");
                else
                    Log.i(TAG, "Save NOT OK");
            }

            if(isImage) {

                Metadata metadata = null;
                try {
                    metadata = ImageMetadataReader.readMetadata(file);
                } catch (ImageProcessingException e) {
                    Log.i(TAG, e.toString());
                }

                String exifDataText = "";
                for (Directory directory : metadata.getDirectories()) {
                    for (Tag tag : directory.getTags()) {
                        //Log.i(TAG, directory.getName() + " : " + tag.getTagName() + " : " + tag.getDescription());
                        exifDataText += directory.getName() + " : " + tag.getTagName() + " : " + tag.getDescription() + "\n";
                    }
                    if (directory.hasErrors()) {
                        for (String error : directory.getErrors()) {
                            Log.i(TAG, "ERROR: " + error);
                        }
                    }
                }

                //Log.i(TAG, exifData);
                //TODO
                exifData.setText(exifDataText);
                exifData.setVisibility(View.VISIBLE);

                imageDownload.setImageBitmap(BitmapFactory.decodeFile(m_path));
                imageDownload.setVisibility(View.VISIBLE);
            }
        }
        return success;
    }

    public void closeFtpCom() throws IOException {
        if (ftpClient != null) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

    public void tryConnect(){
        ftpClient = new FTPClient();
        ftpClient.setDataTimeout(DATA_TIMEOUT_MILLIS);
        try {
            ftpClient.connect(server, port);
            showServerReply(ftpClient);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                Log.i(TAG, "Operation failed. Server reply code: " + replyCode);
                return;
            }
            boolean success = ftpClient.login(user, pass);
            showServerReply(ftpClient);
            if (!success) {
                Log.i(TAG, "Could not login to the server");
            } else {
                if(spewDebug)
                    Log.i(TAG, "LOGGED IN SERVER");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setControlEncoding("UTF-8");
            }
        } catch (IOException ex) {
            Log.i(TAG, "Oops! Something wrong happened");
            Log.i(TAG, ex.toString());
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FtpClient.this);
            // set title
            alertDialogBuilder.setTitle("Oops! Something wrong happened");

            // set dialog message
            alertDialogBuilder.setMessage("Please Try Again!")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            onStop();
                        }
                    });
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
        }
    }

    public int showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                if(spewDebug)
                    Log.i(TAG, "SERVER: " + aReply);
            }
        }
        return ftpClient.getReplyCode();
    }

    //Run task periodically
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            if(isToConnect){
                isToConnect = false;
                tryConnect();
            }
            customHandler.postDelayed(this, 1000);
            //Log.i(TAG, "OneSecond");
            if(readFtp){
                readFtp = false;
                try {
                    getInfo();
                } catch (IOException e) {
                    Log.i(TAG, e.toString());
                }
                drawInterface();
            }
            //Log.i(TAG, "ID: "+currentIdPath);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // do something
            if(!isToViewImage) {
                if (currentIdPath > 0) {
                    pbHeaderProgress.setVisibility(View.VISIBLE);
                    jumpToFolder("../");
                    currentIdPath = currentIdPath - 2;
                    readFtp = true;
                } else {
                    if(spewDebug)
                        Log.i(TAG, "Is in root folder");
                    onStop();
                }
            }
            else
            {
                exifData.setVisibility(View.INVISIBLE);
                imageDownload.setVisibility(View.INVISIBLE);
                isToViewImage = false;
                listView_sys.setVisibility(View.VISIBLE);
            }

            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onStop() {
        try {
            closeFtpCom();
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }
        super.onStop();
        customHandler.removeCallbacks(updateTimerThread);
        this.finish();
    }
}
