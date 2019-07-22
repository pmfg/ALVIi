package pt.lsts.alvii;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Pedro Gonçalves on 7/11/17.
 * LSTS - FEUP
 */

public class OfflineMode extends AppCompatActivity {

    private static final String TAG = "MEU OFFLINEMODE";
    final Context context = this;
    public static Activity fa;
    private final static int MOBILE = 0;
    private final static int WIFI = 1;
    private ProgressBar barWifi;
    private Handler customHandler;
    ImageButton imageButtonLog;
    ImageButton imageButtonFtp;
    private ImageButton imageButtonState;
    private TextView textStateNetwork;
    private TextView textViewWifiBar;
    String ssid;
    NetworkInfo networkInfo;
    ConnectivityManager connManager;
    boolean firstBack = true;
    boolean haveNetwork = false;
    int strengthWifi = 0;
    private TextToSpeech mTts;
    boolean textToSpeech = false;
    boolean isSpeakWifiStateOn = true;
    boolean isSpeakWifiStateOff = true;
    String textInitToSpeak = "ALVIi, is in Offline Mode";
    String[] bufferTextSpeak = new String[30];
    int cntSpeak = 0;
    int cntSpeakId = 0;
    MenuInflater inflater;
    private String lastNumber = "+351";
    private ListView mListView;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;
    ArrayList<String> contactList;
    Cursor cursor;
    int counter;
    AlertDialog alertContact;
    EditText systemNumber;
    private File storageDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "alvii");
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyMgr;
    // Sets an ID for the notification
    int mNotificationId = 001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
                    storageDir.toString(), ""));
        }
        cntSpeak = 0;
        cntSpeakId = 0;
        isSpeakWifiStateOn = true;
        isSpeakWifiStateOff = true;
        mTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    mTts.setLanguage(Locale.UK);
                    textToSpeech = true;
                    //Log.i(TAG, "OK");
                }
            }
        });

        AlviiMain.mainActivity.finish();
        try{
            ImcLocation.fa_ImcLocation.finish();
        }catch (Exception io){}

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_mode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        fa = this;

        textStateNetwork = findViewById(R.id.textStateNetwork);
        barWifi = findViewById(R.id.progressBarWifi);
        barWifi.setMax(100);
        textViewWifiBar = findViewById(R.id.textViewWifiBar);
        textViewWifiBar.setText("0 %");

        addListenerOnButton();
        customHandler = new android.os.Handler();
        customHandler.postDelayed(updateTimerThread,0);

        sendToSpeak(textInitToSpeak, true);

        Drawable drawable=getApplicationInfo().loadIcon(getPackageManager());
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        mBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_extension_black_24dp)
                .setContentTitle("ALVIi")
                .setOngoing(true)
                .setContentText("Running in Offline Mode");

        // Gets an instance of the NotificationManager service
        mNotifyMgr =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    private void sendToSpeak(String text, boolean isToAdd){
        if (isToAdd) {
            bufferTextSpeak[cntSpeak] = text;
            cntSpeak++;
            if (cntSpeak >= 30)
                cntSpeak = 0;
        } else {
            if (!mTts.isSpeaking() && textToSpeech && cntSpeak != cntSpeakId) {
                mTts.speak(bufferTextSpeak[cntSpeakId], TextToSpeech.QUEUE_FLUSH, null);
                cntSpeakId++;
                if (cntSpeakId >= 30)
                    cntSpeakId = 0;
            }
        }
    }

    public void exitApp() {
        mNotifyMgr.cancel(mNotificationId);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
        if(mTts !=null){
            mTts.stop();
            mTts.shutdown();
        }
        this.finish();
        System.exit(0);
    }

    //Run task periodically
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            customHandler.postDelayed(this, 1000);
            if(checkStateNetwork()) {
                haveNetwork = true;
                textStateNetwork.setText(getCurrentSsid(OfflineMode.this));
                barWifi.setProgress(strengthWifi);
                textViewWifiBar.setText(""+strengthWifi+" %");
                imageButtonState.setImageResource(R.drawable.online_wifi);
                if(isSpeakWifiStateOn) {
                    sendToSpeak("wifi is on", true);
                    isSpeakWifiStateOn = false;
                    isSpeakWifiStateOff = true;
                }
                else {
                    if (textToSpeech)
                        sendToSpeak("", false);
                }
            }
            else {
                textStateNetwork.setText("Wifi Offline");
                barWifi.setProgress(0);
                textViewWifiBar.setText("");
                imageButtonState.setImageResource(R.drawable.offline_wifi);
                haveNetwork = false;
                if(isSpeakWifiStateOff) {
                    sendToSpeak("wifi is off", true);
                    isSpeakWifiStateOff = false;
                    isSpeakWifiStateOn = true;
                }
                else{
                    if(textToSpeech)
                        sendToSpeak("", false);
                }
            }
        }
    };

    private boolean checkStateNetwork(){
        return (isWifiAvailable() && isNetworkAvailable());
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

    public void addListenerOnButton() {
        imageButtonLog = findViewById(R.id.imageButtonLog);
        imageButtonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                firstBack = true;
                //Toast.makeText(OfflineMode.this, "imageButtonLog is clicked!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OfflineMode.this, ReviewOldLogs.class);
                startActivity(intent);
            }
        });

        imageButtonFtp = findViewById(R.id.imageButtonDownload);
        imageButtonFtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                firstBack = true;
                if(haveNetwork) {
                    //Toast.makeText(OfflineMode.this, "imageButtonFtp is clicked!", Toast.LENGTH_SHORT).show();
                    startFtp("0.0.0.0", "no name");
                }
                else{
                    Toast.makeText(OfflineMode.this, "No network available!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageButtonState = findViewById(R.id.imageButtonWifiState);
        imageButtonState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                firstBack = true;
                if(haveNetwork) {
                    //Toast.makeText(OfflineMode.this, "imageButtonState is clicked!", Toast.LENGTH_SHORT).show();
                    mNotifyMgr.cancel(mNotificationId);
                    Intent intent = new Intent(OfflineMode.this, ImcLocation.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(OfflineMode.this, "No network available!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    public String getCurrentSsid(Context context) {
        ssid = "null";
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo.isConnected()) {
            if (networkInfo.getType() == WIFI) {
                final WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                    ssid = connectionInfo.getSSID() + " - IP: " + Formatter.formatIpAddress(connectionInfo.getIpAddress());
                    strengthWifi = WifiManager.calculateSignalLevel(connectionInfo.getRssi(), 101);
                }
            } else if (networkInfo.getType() == MOBILE) {
                ssid = "Using mobile internet: " + networkInfo.getExtraInfo();
            }
        }
        return ssid;
    }

    public void startFtp(final String ip, final String system) {
        LayoutInflater inflater=OfflineMode.this.getLayoutInflater();
        View layout=inflater.inflate(R.layout.dialog_ftp,null);
        final EditText systemIp = layout.findViewById(R.id.dialogIp);
        final EditText systemPort = layout.findViewById(R.id.dialogPort);
        systemIp.setText(ip);
        final String portFtp = "30021";
        systemPort.setText(portFtp);

        AlertDialog.Builder builder1 = new AlertDialog.Builder(OfflineMode.this);
        builder1.setMessage("Values Ftp!");
        builder1.setCancelable(true);

        builder1.setPositiveButton("Submit Config", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                Intent intent = new Intent(OfflineMode.this, FtpClient.class);
                Bundle b = new Bundle();
                b.putString("BUNDLE_IP", systemIp.getText().toString());
                b.putString("BUNDLE_SYSTEM_PORT", systemPort.getText().toString());
                b.putString("BUNDLE_SYSTEM_NAME", system);
                intent.putExtras(b);
                intent.putExtra("EXTRA_LOCATION_NAME", "ftp");
                startActivity(intent);
            }
        });
        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder1.setView(layout);
        final AlertDialog alert11 = builder1.create();
        alert11.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alert11.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                alert11.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
            }
        });
        alert11.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        inflater = getMenuInflater();
        inflater.inflate(R.menu.offlinemenu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sendsmsoffline:
                startSendSMS();
                break;
            case R.id.action_ripples_map:
                Intent intent_web = new Intent(this, ViewWebPage.class);
                startActivity(intent_web);
                break;
            default:
                break;
        }
        return true;
    }

    private void startLoadContact(){
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();

        LayoutInflater inflater = OfflineMode.this.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.activity_contacts, null);

        final AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Contacts");
        builder1.setCancelable(true);
        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder1.setView(layout);

        mListView = layout.findViewById(R.id.listcontacts);
        updateBarHandler =new Handler();
        // Since reading contacts takes more time, let's run it on a separate thread.
        new Thread(new Runnable() {

            @Override
            public void run() {
                getContacts();
            }
        }).start();
        // Set onclicklistener to the list item.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] textStr = contactList.get(position).split("\\r\\n|\\n|\\r");
                String number = textStr[2].replace("-", "");
                String number2 = number.replace(" ", "");
                if(number2.contains("+"))
                    systemNumber.setText(number2);
                else
                    systemNumber.setText("+351"+number2);

                alertContact.dismiss();
            }
        });

        alertContact = builder1.create();
    }

    private void startSendSMS(){
        LayoutInflater inflater = OfflineMode.this.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_sms, null);
        systemNumber = layout.findViewById(R.id.dialogNumber);
        systemNumber.setText(lastNumber);
        final ImageButton buttonContact = layout.findViewById(R.id.imageButtonContact);
        buttonContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startLoadContact();
            }
        });
        final RadioGroup radioGroup = layout.findViewById(R.id.radioGroup);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Sms Command!");
        builder1.setCancelable(true);
        builder1.setPositiveButton("Send Sms", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                //RadioButton radioButton = layout.findViewById(selectedId);
                //Toast.makeText(ImcLocation.this, radioButton.getText(), Toast.LENGTH_SHORT).show();
                dialog.cancel();
                lastNumber = systemNumber.getText().toString();
                switch (selectedId){
                    case R.id.radioButtonPos:
                        sendSMS(systemNumber.getText().toString(), "pos");
                        break;
                    case R.id.radioButtonAbort:
                        sendSMS(systemNumber.getText().toString(), "abort");
                        break;
                    case R.id.radioButtonGSMOn:
                        sendSMS(systemNumber.getText().toString(), "gsm true");
                        break;
                    case R.id.radioButtonGSMOff:
                        sendSMS(systemNumber.getText().toString(), "gsm false");
                        break;
                    default:
                        Toast.makeText(OfflineMode.this, "Sms Cmd: No option selected!!!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder1.setView(layout);
        final AlertDialog alert11 = builder1.create();
        alert11.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alert11.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                alert11.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            }
        });
        alert11.show();
    }

    private boolean sendSMS(String toNum, String smsText) {
        try
        {
            Toast.makeText(getApplicationContext(),"Number: "+ toNum + "  #  Text: " + smsText,Toast.LENGTH_SHORT).show();
            String SENT = "SMS_SENT";
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
            registerReceiver(new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context arg0, Intent arg1)
                {
                    int resultCode = getResultCode();
                    switch (resultCode)
                    {
                        case Activity.RESULT_OK:
                            Toast.makeText(getBaseContext(), "SMS sent successfully",Toast.LENGTH_LONG).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            Toast.makeText(getBaseContext(), "SMS Fail: Generic failure",Toast.LENGTH_LONG).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            Toast.makeText(getBaseContext(), "SMS Fail: No service",Toast.LENGTH_LONG).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            Toast.makeText(getBaseContext(), "SMS Fail: Null PDU",Toast.LENGTH_LONG).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            Toast.makeText(getBaseContext(), "SMS Fail: Radio off",Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }, new IntentFilter(SENT));

            try{
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(toNum, null, smsText, sentPI, null);
            }catch (Exception e){
                Log.i(TAG, ""+e);
            }
            return true;
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"SMS faild, please try again later!",Toast.LENGTH_SHORT).show();
            Log.i(TAG, ""+e);
        }
        return false;
    }

    public void getContacts() {
        contactList = new ArrayList<String>();
        String phoneNumber = null;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        StringBuffer output;
        ContentResolver contentResolver = getContentResolver();
        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);
        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {
            counter = 0;
            while (cursor.moveToNext()) {
                output = new StringBuffer();
                // Update the progress message
                updateBarHandler.post(new Runnable() {
                    public void run() {
                        pDialog.setMessage("Reading contacts : " + counter++ + "/" + cursor.getCount());
                    }
                });
                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    output.append("\n" + name);
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        output.append("\n" + phoneNumber);
                    }
                    phoneCursor.close();
                    // Add the contact to the ArrayList
                    contactList.add(output.toString());
                }
            }
            // ListView has to be updated using a ui thread
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, R.id.text1, contactList);
                    mListView.setAdapter(adapter);
                    alertContact.show();
                }
            });
            // Dismiss the progressbar after 500 millisecondds
            updateBarHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    pDialog.cancel();
                }
            }, 500);
        }
    }
}
