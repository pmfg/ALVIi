package pt.lsts.alvii;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.util.Locale;
import java.util.Vector;

import pt.lsts.accl.event.EventSystemDisconnected;
import pt.lsts.accl.event.EventSystemVisible;
import pt.lsts.accl.sys.Sys;
import pt.lsts.accl.util.pos.LatLng;
import pt.lsts.accl.util.pos.Position;
import pt.lsts.imc.Announce;
import pt.lsts.imc.CpuUsage;
import pt.lsts.imc.EntityState;
import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.FuelLevel;
import pt.lsts.imc.Heartbeat;
import pt.lsts.imc.PlanControlState;
import pt.lsts.imc.PlanDB;
import pt.lsts.imc.PlanDBInformation;
import pt.lsts.imc.PlanDBState;
import pt.lsts.imc.StorageUsage;

/**
 * Created by jloureiro on 02-07-2015.
 * Modified by PGonçalves on 07-07-2017
 */
public class AcclBusListenner extends Service {

    class SystemInfo {
        public Sys[] system_profile = new Sys[100];
        public String[] system_names = new String[100];
        public String[] system_ip = new String[100];
        public Position[] pos = new Position[100];
        public boolean[] isAlive = new boolean[100];
        public String[][] entity_name = new String[100][100];
        public String[][] entity_mode = new String[100][100];
        public String[][] entity_state = new String[100][100];
        public int[] entity_cnt = new int[100];
        public int[] cpuUsage = new int[100];
        public int[] hddUsage = new int[100];
        public double[] heading = new double[100];
        public double[] fuelUsage = new double[100];
        public double[] speed = new double[100];
        public String[] planControlState = new String[100];
        public String[][] planList = new String[100][100];
        public int[] numberPlanSystem = new int[100];
        public boolean[] haveHeartBeat = new boolean[100];
        public String[][] planInfoRun = new String[100][2];
        private Announce[] m_buffer_announce = new Announce[10];
    }

    public static final String TAG = "MEU AcclBusListenner";
    public SystemInfo m_system_Info = new SystemInfo();
    public int number_system = 0;
    private String  selectedSystem = null;
    private int counterAnounce;

    String[] bufferTextSpeak = new String[30];
    int cntSpeak = 0;
    int cntSpeakId = 0;
    private TextToSpeech mTts;
    boolean textToSpeech = false;

    public void init_systm_info() {
        cntSpeak = 0;
        cntSpeakId = 0;
        mTts = new TextToSpeech(ImcLocation.fa_ImcLocation, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    mTts.setLanguage(Locale.UK);
                    //mTts.setLanguage(Locale.CANADA);
                    textToSpeech = true;
                    //Log.i(TAG, "OK");
                }
            }
        });
        for (int i = 0; i < 100; i++) {
            m_system_Info.isAlive[i] = false;
            m_system_Info.entity_cnt[i] = 0;
            m_system_Info.planInfoRun[i][0] = null;
            m_system_Info.planInfoRun[i][1] = null;
        }
        counterAnounce = 0;
    }

    public void flushSpeek(){
        sendToSpeak("", false);
    }

    public void sendToSpeak(String text, boolean isToAdd){
        text = text.replace("-", " ");
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

    public void cleanEntityBuffer(){
        for (int i = 0; i < 100; i++) {
            m_system_Info.entity_cnt[i] = 0;
        }
    }

    public void setSelectedSystem(String sysName){
        selectedSystem = sysName;
        /*if(selectedSystem != null)
            Log.i(TAG, "Selecting "+selectedSystem+" for teleoperation");
        else
            Log.i(TAG, "Resenting selected system for teleoperation");*/
    }

    @Subscribe
    public void on(EventSystemVisible event) {
        String m_system_name = event.getSys().getName();
        String m_system_type = event.getSys().getSysType().name();
        Log.i(TAG, m_system_name + ", is now visible (" + m_system_type + ") IP: " + event.getSys().getIpAddress());
        if (number_system == 0) {
            m_system_Info.system_names[0] = m_system_name;
            m_system_Info.system_ip[0] = event.getSys().getIpAddress();
            m_system_Info.isAlive[0] = true;
            m_system_Info.system_profile[0] = event.getSys();
            number_system++;
            if(!event.getSys().getSysType().toString().equals("CCU") && !event.getSys().getSysType().toString().equals("MOBILESENSOR"))
                sendToSpeak(m_system_name + ", is online", true);
        } else {
            boolean jump_sys = false;
            for (int i = 0; i < number_system; i++) {
                if (m_system_Info.system_profile[i].getName().equals(m_system_name)) {
                    //Log.i(TAG, "JUMP: "+m_system_name);
                    m_system_Info.isAlive[i] = true;
                    jump_sys = true;
                    ImcLocation.back_number_sys = 0;
                    if(!event.getSys().getSysType().toString().equals("CCU") && !event.getSys().getSysType().toString().equals("MOBILESENSOR"))
                        sendToSpeak(m_system_name + ", is online", true);
                }
            }

            if (!jump_sys) {
                //Log.i(TAG, "NOT JUMP: "+m_system_name);
                m_system_Info.system_names[number_system] = m_system_name;
                m_system_Info.system_ip[number_system] = event.getSys().getIpAddress();
                m_system_Info.isAlive[number_system] = true;
                m_system_Info.system_profile[number_system] = event.getSys();
                number_system++;
                if(!event.getSys().getSysType().toString().equals("CCU") && !event.getSys().getSysType().toString().equals("MOBILESENSOR"))
                    sendToSpeak(m_system_name + ", is online", true);
            }
        }
    }

    public String[] getSystem_names() {
        return m_system_Info.system_names;
    }

    public String[] getSystem_ip() {
        return m_system_Info.system_ip;
    }

    public String getSystemIpByName(String system_name) {
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(system_name)) {
                return m_system_Info.system_ip[i];
            }
        }
        return null;
    }

    public int getNumber_system() {
        return number_system;
    }

    public boolean isAlive(String sys_name) {
        boolean result = false;
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sys_name)) {
                result = m_system_Info.isAlive[i];
            }
        }
        return result;
    }

    public Sys getSysProfile(String system_name) {
        Sys sys = new Sys();
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(system_name)) {
                sys = m_system_Info.system_profile[i];
            }
        }
        return sys;
    }

    public Position getPositionSystem(String system_name) {
        Position pos = null;
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(system_name)) {
                return m_system_Info.pos[i];
            }
        }
        return pos;
    }

    @Subscribe
    public void on(EstimatedState state) {
        //Log.i(TAG, state.getSourceName());
        for (int i = 0; i < number_system; i++) {
            //Log.i(TAG, m_system_Info.system_names[i]+ " : "+state.getSourceName()+"AQUI p: "+i);
            if (m_system_Info.system_names[i].equals(state.getSourceName())) {
                m_system_Info.pos[i] = Position.calcPositionFromEstimatedState(state);
                m_system_Info.speed[i] = state.getU();
                m_system_Info.heading[i] = Math.toDegrees(state.getPsi());
                //Log.i(TAG, state.getSourceName() + " Lat: " + m_system_Info.pos[i].getLatitude() + " Lon:" + m_system_Info.pos[i].getLongitude());
                break;
            }
        }
    }

    public double getSpeed(String sysName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName))
                if(m_system_Info.speed[i] < 0)
                    return 0;
                else
                    return m_system_Info.speed[i];
        }
        return -1;
    }

    public double getHeadingSys(String sysName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName))
                return m_system_Info.heading[i];
        }
        return -1;
    }

    public int getEntityCnt(String sysName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName))
                return m_system_Info.entity_cnt[i];
        }
        return -1;
    }

    public String getSysType(String sysName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName))
                return m_system_Info.system_profile[i].getSysType().toString();
        }
        return null;
    }

    public String[] getEntityNames(String sysName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName))
                return m_system_Info.entity_name[i];
        }
        return null;
    }

    public String[] getEntityState(String sysName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName))
                return m_system_Info.entity_state[i];
        }
        return null;
    }

    public String[] getEntityMode(String sysName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName))
                return m_system_Info.entity_mode[i];
        }
        return null;
    }

    @Subscribe
    public void on(EntityState state) {
        int pos_lock = 0;
        for (int i = 0; i < number_system; i++) {
            //TODO
            //Log.i(TAG, state.toString());
            //Log.i(TAG, state.getSourceName()+" : "+state.getEntityName()+" : "+state.getState()+ " : "+state.getDescription());

            if (m_system_Info.system_names[i].equals(state.getSourceName()) && state.getEntityName() != null) {

                int cnt = m_system_Info.entity_cnt[i];
                //Log.i(TAG,"CNT: "+cnt+ " - ID: "+i);
                if(cnt == 0){
                    m_system_Info.entity_name[i][0] = state.getEntityName();
                    m_system_Info.entity_mode[i][0] = state.getState().toString();
                    m_system_Info.entity_state[i][0] = state.getDescription();
                    m_system_Info.entity_cnt[i]++;
                }
                else {
                    boolean dontHave = true;
                    for (int t = 0; t < cnt; t++) {
                        if(m_system_Info.entity_name[i][t].equals(state.getEntityName())) {
                            dontHave = false;
                            pos_lock = t;
                        }
                    }

                    if(dontHave){
                        //Log.i(TAG,"A: "+i+" - B: "+cnt);
                        m_system_Info.entity_name[i][cnt] = state.getEntityName();
                        m_system_Info.entity_mode[i][cnt] = state.getState().toString();
                        m_system_Info.entity_state[i][cnt] = state.getDescription();
                        m_system_Info.entity_cnt[i]++;
                    }
                    else
                    {
                        m_system_Info.entity_mode[i][pos_lock] = state.getState().toString();
                        m_system_Info.entity_state[i][pos_lock] = state.getDescription();
                    }
                }
                break;
            }
        }

        //if(state.getSourceName().toString().equals("x8-07"))
        //Log.i(TAG, state.getSourceName()+" : "+state.getEntityName()+" : "+state.getState()+ " : "+state.getDescription());
    }

    @Subscribe
    public void on(Announce state) {
        if(number_system < 1)
            return;

        if(!state.getSysType().toString().equals("CCU")){
            //Log.i(TAG, state.getServices().toString());
            return;
        }

        if(counterAnounce >= 9)
            counterAnounce = 0;

        m_system_Info.m_buffer_announce[counterAnounce] = state;
        counterAnounce++;
    }

    public void updateCcu() {
        for(int i = 0; i < 10; i++){
            if(m_system_Info.m_buffer_announce[i] != null){
                for (int t = 0; t < number_system; t++) {
                    if (m_system_Info.system_names[t].equals(m_system_Info.m_buffer_announce[i].getSourceName())) {
                        try {
                            double t_lat = Math.toDegrees(m_system_Info.m_buffer_announce[i].getLat());
                            double t_lon = Math.toDegrees(m_system_Info.m_buffer_announce[i].getLon());
                            m_system_Info.pos[t] = Position.calcPosition(new LatLng(t_lat, t_lon));
                        }
                        catch (Exception e){
                            Log.i(TAG, e.toString());
                        }
                        break;
                    }
                }
            }
        }
    }


    @Subscribe
    public void on(EventSystemDisconnected event) {
        Log.i(TAG, event.toString());
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(event.getSys().getName())) {
                m_system_Info.isAlive[i] = false;
                m_system_Info.entity_cnt[i] = 0;
                ImcLocation.back_number_sys = 0;
                if (!event.getSys().getSysType().toString().equals("CCU") && !event.getSys().getSysType().toString().equals("MOBILESENSOR"))
                    sendToSpeak(m_system_Info.system_names[i] + ", is offline", true);
            }
        }
    }

    @Subscribe
    public void on(CpuUsage state) {
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(state.getSourceName())) {
                m_system_Info.cpuUsage[i] = state.getValue();
                //Log.i(TAG, state.getSourceName() + " CPU: " + m_system_Info.cpuUsage[i]);
                break;
            }
        }
    }

    public int getCpuUsage(String sys_name){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sys_name)) {
                return m_system_Info.cpuUsage[i];
            }
        }
        return -1;
    }

    @Subscribe
    public void on(Heartbeat state) {
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(state.getSourceName())) {
                m_system_Info.haveHeartBeat[i] = true;
                //Log.i(TAG, state.getSourceName() + " Heartbeat: " + m_system_Info.haveHeartBeat[i]);
                break;
            }
        }
    }

    public boolean getHeartBeat(String sys_name){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sys_name)) {
                return m_system_Info.haveHeartBeat[i];
            }
        }
        return false;
    }

    public void clearHeartBeat(String sys_name){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sys_name)) {
                m_system_Info.haveHeartBeat[i] = false;
            }
        }
    }

    @Subscribe
    public void on(StorageUsage state) {
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(state.getSourceName())) {
                m_system_Info.hddUsage[i] = state.getValue();
                //Log.i(TAG, state.getSourceName() + " HDD: " + m_system_Info.hddUsage[i]);
                break;
            }
        }
    }

    public int getHddUsage(String sys_name){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sys_name)) {
                if(m_system_Info.hddUsage[i] > 0)
                    return 100 - m_system_Info.hddUsage[i];
                else
                    return 0;
            }
        }
        return -1;
    }

    @Subscribe
    public void on(FuelLevel state) {
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(state.getSourceName())) {
                m_system_Info.fuelUsage[i] = state.getValue();
                //Log.i(TAG, state.getSourceName() + " HDD: " + m_system_Info.hddUsage[i]);
                break;
            }
        }
    }

    public double getFuelUsage(String sys_name){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sys_name)) {
                if(m_system_Info.fuelUsage[i] > 0)
                    return m_system_Info.fuelUsage[i];
                else
                    return 0;
            }
        }
        return -1;
    }

    @Subscribe
    public void on(PlanControlState state) {
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(state.getSourceName())) {
                m_system_Info.planControlState[i] = state.getPlanId();
                m_system_Info.planInfoRun[i][0] = state.getPlanId();
                m_system_Info.planInfoRun[i][1] = state.getStateStr();
                //Log.i(TAG, state.getSourceName() + " Plan: " + m_system_Info.planControlState[i]);
                break;
            }
        }
    }

    public boolean isRunnigPlan(String sysName, String planName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName)) {
                if(m_system_Info.planInfoRun[i][0].equals(planName)){
                    if(m_system_Info.planInfoRun[i][1].equals("EXECUTING"))
                        return true;
                    else
                        return false;
                }
            }
        }
        return false;
    }
    public String getPlanControlState(String sys_name){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sys_name)) {
                return m_system_Info.planControlState[i];
            }
        }
        return null;
    }

    @Subscribe
    public void on(PlanDB state) {
        try {
            for (int i = 0; i < number_system; i++) {
                if (m_system_Info.system_names[i].equals(state.getSourceName())) {
                    PlanDBState pl = (PlanDBState) state.getArg();
                    m_system_Info.numberPlanSystem[i] = pl.getPlanCount();
                    int cnt_plan = m_system_Info.numberPlanSystem[i];
                    Vector<PlanDBInformation> pli = pl.getPlansInfo();
                    int cnt = 0;
                    while (cnt < cnt_plan) {
                        //Log.i(TAG, pli.get(cnt).getPlanId());
                        m_system_Info.planList[i][cnt] = pli.get(cnt).getPlanId();
                        cnt++;
                    }
                    while (cnt < 100 - cnt_plan) {
                        m_system_Info.planList[i][cnt] = null;
                        cnt++;
                    }
                }
            }
        }catch (Exception io){
            //Log.i(TAG, ""+io);
        }
    }

    public String[] getPlanList( String sysName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName)) {
                return m_system_Info.planList[i];
            }
        }
        return null;
    }

    public int getNumberPlanSystem( String sysName){
        for (int i = 0; i < number_system; i++) {
            if (m_system_Info.system_names[i].equals(sysName)) {
                return m_system_Info.numberPlanSystem[i];
            }
        }
        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}