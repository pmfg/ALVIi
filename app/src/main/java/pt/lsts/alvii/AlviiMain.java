package pt.lsts.alvii;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Pedro Gonçalves on 7/11/17.
 * LSTS - FEUP
 */
public class AlviiMain extends Activity {

    private static final String TAG = "MEU MAIN";
    public boolean result_permission = true;
    final Context context = this;
    private Handler customHandler;
    boolean firstBack = true;
    boolean isInitDone;
    private File storageDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "alvii");
    ImageView mImageView_logo;
    ImageView mImageView_logo_0;
    ImageView mImageView_logo_1;
    private TextView perc;
    private int cnt_perc;
    private boolean startApp;
    private boolean isStop;
    public static Activity mainActivity;
    private ProgressBar bar;

    //Run task periodically
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            if(!isStop){
                customHandler.postDelayed(this, 105 - cnt_perc);
                String text = ""+cnt_perc+" %";
                perc.setText(text);
                cnt_perc = cnt_perc + 5;
                bar.setProgress(cnt_perc);
                if(cnt_perc > 100) {
                    if(!startApp){
                        startApp = true;
                        isStop = true;
                        startApp();
                    }
                }
            }
            else {
                customHandler.postDelayed(this, 1000);
            }
        }
    };

    public void startApp(){
        Intent intent = new Intent(this, ImcLocation.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
                    storageDir.toString(), ""));
        }
        mainActivity = this;
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_alvii_main);
        cnt_perc = 0;

        isInitDone = false;
        startApp = false;
        isStop = false;

        if(Build.VERSION.SDK_INT <= 22){
            checkConnections();
            //customHandler = new android.os.Handler();
            //customHandler.postDelayed(updateTimerThread,0);
        }
        else {
            requestForSpecificPermission();
        }

        mImageView_logo = findViewById(R.id.imageLogo);
        mImageView_logo.setImageResource(R.drawable.logo);
        mImageView_logo_0 = findViewById(R.id.imageLogo_0);
        mImageView_logo_0.setImageResource(R.drawable.logo_0);
        mImageView_logo_1 = findViewById(R.id.imageLogo_1);
        mImageView_logo_1.setImageResource(R.drawable.logo_1);
        perc = findViewById(R.id.textPerc);
        bar = findViewById(R.id.progressBar2);
        bar.setMax(100);
        bar.setIndeterminate(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(firstBack){
                firstBack = false;
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG).show();
            }
            else
            {
                exitApp();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exitApp() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
        this.finish();
        System.exit(0);
    }

    private boolean isWifiAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return  wifi.isConnected();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void requestForSpecificPermission() {
        int PERMISSION_ALL = 101;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS,
                //Manifest.permission.CAMERA,
                Manifest.permission.VIBRATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET};

        hasPermissions(this, PERMISSIONS);
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
    }

    public void hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                ActivityCompat.checkSelfPermission(context, permission);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == -1)
                result_permission = false;
        }
        if (!result_permission) {
            close_app();
        } else {
            checkConnections();
        }
    }

    private void checkConnections(){
        if (isWifiAvailable() && isNetworkAvailable()) {
            boolean success;
            if (!storageDir.exists()) {
                Log.i(TAG, "Creating foler");
                success = storageDir.mkdirs();
                if (success)
                    Log.i(TAG, "Folder created");
                else {
                    Log.i(TAG, "Error creating folder");
                    showErrorInfo("Error creating folder");
                }
            }
            isInitDone = true;
            customHandler = new android.os.Handler();
            customHandler.postDelayed(updateTimerThread, 0);
        }
        else {
            boolean success;
            if (!storageDir.exists()) {
                Log.i(TAG, "Creating foler");
                success = storageDir.mkdirs();
                if (success)
                    Log.i(TAG, "Folder created");
                else {
                    Log.i(TAG, "Error creating folder");
                    showErrorInfo("Error creating folder");
                }
            }
            //showErrorInfo("Please turn on Wifi");
            Toast.makeText(this, "No network available...\nPlease turn on Wifi", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Going in Offline Mode !!!", Toast.LENGTH_SHORT).show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(AlviiMain.this, OfflineMode.class);
                    startActivity(intent);
                }
            }, 2000);
        }
    }

    public void close_app() {
        //Log.i(TAG, "close app");
        showErrorInfo("Please accept the permissions!!!\nIt is necessary to accept the permissions to run da app!!!");
    }

    void showErrorInfo(String text){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Error !!!");
        alertDialogBuilder.setMessage(text)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AlviiMain.this.finish();
                        System.exit(0);
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
