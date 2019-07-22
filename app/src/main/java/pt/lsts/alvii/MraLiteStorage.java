package pt.lsts.alvii;

import android.content.Context;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.lsf.LsfIndex;

/**
 * Created by pedro on 9/15/17.
 * LSTS - FEUP
 */

class MraLiteStorage {

    private static final String TAG = "MEU MraLiteStorage";
    private LsfIndex m_index;
    private Context m_context;
    private String messageList[] = new String[1024];
    private int numberOfMessage = 0;
    private boolean isFinish;
    private int cntListLog = 0;
    private int cntThread;
    private String listImcMessagesXMl[] = new String[1024];
    int cntFullIMC;
    int cntStateImcMsg;

    public MraLiteStorage(Context context) {
        m_context = context;
        isFinish = false;
        cntThread = 0;
        cntStateImcMsg = 0;
        cntFullIMC = 0;
    }

    public void initMraLiteStorage(){
        isFinish = false;
        cntThread = 0;
    }

    public long getProcessStageValue(){;
        return cntThread ;
    }

    public int getNumberMessages(){
        return numberOfMessage;
    }

    public int getNumberOfListMsg() { return cntListLog; }

    public boolean isFinish(){
        return isFinish;
    }

    public String[] getListOfMessages(){
        return messageList;
    }

    public void indexListOfMessage(LsfIndex index, File path) {
        isFinish = false;
        m_index = index;
        numberOfMessage = m_index.getNumberOfMessages();
        getListIMCMesages(index, path);
    }

    private void getListIMCMesages(LsfIndex index, File source){
        XmlPullParserFactory pullParserFactory;
        int cnt = 0;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            InputStream in_s = new FileInputStream(new File(source.getParent(), "imc.xml"));
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
            int eventType=parser.getEventType();
            while(eventType!=XmlPullParser.END_DOCUMENT){
                String name;
                switch(eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if(name.equals("message")){
                            listImcMessagesXMl[cnt++] = parser.getAttributeValue(null,"abbrev");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType=parser.next();
            }
        } catch (XmlPullParserException e) {
            Log.i(TAG, "XmlPullParserException: ", e);
        } catch (IOException e) {
            Log.i(TAG, "IOException: ", e);
        }
        getListIndexImc(index, source, listImcMessagesXMl, cnt);
    }

    public int getCntListFullImcMsg(){
        return cntFullIMC;
    }

    public int getCntStateImcMsg(){
        return cntStateImcMsg;
    }

    public void getListIndexImc(LsfIndex index, File path, String [] listImcMessages, int cntMessages){
        cntFullIMC = cntMessages;
        int cnt = 0;
        for(int i = 0; i < cntMessages; i++) {
            if (index.containsMessagesOfType(listImcMessages[i])) {
                messageList[cnt++] = listImcMessages[i];
                cntThread = cnt - 1;
            }
            cntStateImcMsg++;
        }
        cntListLog = cnt - 1;

        File root = new File(path.getParent());
        if (!root.exists()) {
            root.mkdirs();
        }
        FileWriter writer;
        try {
            writer = new FileWriter(new File(root, "indexMessageList.stackIndex"));
            for (int i = 0; i < cntListLog; i++)
                writer.append(messageList[i]+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        isFinish = true;
    }

    public void getListMessgeByOldIndex(File source){
        isFinish = false;
        int cnt = 0;
        File gpxfile = new File(source.getParent(), "indexMessageList.stackIndex");
        FileInputStream is = null;
        try {
            is = new FileInputStream(gpxfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(line != null){
            messageList[cnt] = line;
            cnt++;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cntListLog = cnt;
        isFinish = true;
    }

            /*double endTime = index.getEndTime();
        double startTime = index.getStartTime();
        double pivot = (endTime + startTime) / 2.0;
        Log.i(TAG,""+endTime+" --> "+ pivot+ " --> "+ startTime);

        for (int i = index.getFirstMessageOfType(EntityState.ID_STATIC); i != -1; i = index.getNextMessageOfType(EntityState.ID_STATIC, i)) {
            Log.i(TAG,index.getSystemName(index.sourceOf(i))+", "+index.entityOf(i)+", "+index.hashOf(i));
        }*/

    /*int cnt_number_of = 0;

        while(state_msg != -1)
        {
            state_msg = index.getNextMessageOfType(EntityState.ID_STATIC, state_msg);
            Log.i(TAG, "number: " + state_msg);
            try {
                Log.i(TAG,""+index.getMessage(state_msg, EntityState.class));
                cnt_number_of++;
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateBarHandler.post(new Runnable() {
                public void run() {
                    if(state_msg >= 0)
                        pDialog.setMessage("Reading Log - Msg " + state_msg + " of " + cnt);
                }
            });
        }*/

        /*for(state_msg = 0; state_msg < cnt; state_msg++){
            //Log.i(TAG, index.getMessage(state_msg).getAbbrev());
            //m_mra_storage.addMsg(index.getMessage(state_msg));
            updateBarHandler.post(new Runnable() {
                public void run() {
                    if(state_msg >= 0)
                        pDialog.setMessage("Reading Log - Msg " + state_msg + " of " + cnt);
                }
            });
        }*/

    //Log.i(TAG, "total: "+cnt_number_of);
    // Dismiss the progressbar after 500 millisecondds

    //Log.i(TAG,""+index.getNextMessageOfType(EntityState.ID_STATIC, 820));
    //Log.i(TAG,""+index.getMessageBeforeOrAt("EntityState", index.getNumberOfMessages(), pivot));
}

