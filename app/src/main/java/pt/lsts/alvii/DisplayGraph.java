package pt.lsts.alvii;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pt.lsts.imc.IMCDefinition;
import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.lsf.LsfIndex;

/**
 * Created by pedro on 9/20/17.
 * LSTS - FEUP
 */

public class DisplayGraph extends AppCompatActivity {
    final Context context = this;
    private String TAG = "MEU DisplayGraph";
    private String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "alvii";
    private File storageDir = new File(path);
    private LsfIndex m_index;
    private File source;
    private String imcMessageName;
    private String entityLabel[] = new String[1024];
    GraphView graph;
    int color[] = new int[10];
    Button button;
    private Handler updateBarHandler;
    private ProgressDialog pDialog;
    private Handler customHandler;
    boolean showLoading = true;
    boolean showButton;

    //Run task periodically
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            if(showLoading)
                pDialog.setMessage("Reading Log..."+new Date().getTime());
            else {
                pDialog.dismiss();
                if(showButton)
                    button.setVisibility(View.VISIBLE);
                else
                    button.setVisibility(View.INVISIBLE);
            }

            customHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
                    storageDir.toString(), ""));
        }
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.mra_plot_graph);
        getSupportActionBar().hide();
        Bundle b = getIntent().getExtras();
        final String path = b.getString("BUNDLE_INDEX_PATH");
        imcMessageName  = b.getString("BUNDLE_IMCMESSAGE");
        source = new File(path);
        try {
            m_index = new LsfIndex(source, IMCDefinition.getInstance());
        } catch (Exception e) {
            Log.i(TAG, "" + e);
        }

        customHandler = new android.os.Handler();
        showLoading = true;
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Reading Log...");
        pDialog.setCancelable(false);
        pDialog.show();
        updateBarHandler =new Handler();
        customHandler.postDelayed(updateTimerThread,0);
        button = (Button) findViewById(R.id.buttonseries);
        showButton = false;

        getEntityInfo("EntityInfo");

        new Thread(new Runnable() {
            @Override
            public void run() {
                graph = (GraphView) findViewById(R.id.graph);
                graph.getViewport().setXAxisBoundsManual(true);
                graph.getViewport().setYAxisBoundsManual(true);
                graph.getViewport().setScalable(true);
                graph.getViewport().setScalableY(true);
                graph.getLegendRenderer().setVisible(true);
                graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

                color[0] = Color.RED;
                color[1] = Color.BLUE;
                color[2] = Color.CYAN;
                color[3] = Color.GREEN;
                color[4] = Color.BLACK;
                color[5] = Color.YELLOW;
                color[6] = Color.LTGRAY;
                color[7] = Color.DKGRAY;
                color[8] = Color.MAGENTA;
                color[9] = Color.GRAY;

                //TODO
                if(imcMessageName.equals("Rpm"))
                    plotRpm();
                else if(imcMessageName.equals("SetThrusterActuation"))
                    plotSetThrusterActuation();
                else if(imcMessageName.equals("EulerAngles"))
                    plotEulerAngles();
                else if(imcMessageName.equals("Voltage"))
                    plotVoltage();
                else if(imcMessageName.equals("Temperature"))
                    plotTemperature();
                else if(imcMessageName.equals("Current"))
                    plotCurrent();
                else if(imcMessageName.equals("FuelLevel"))
                    plotFuelLevel();
                else if(imcMessageName.equals("CpuUsage"))
                    plotCpuUsage();
                else {
                    Toast.makeText(context, "Message not supported!", Toast.LENGTH_SHORT).show();
                    showLoading = false;
                    finish();
                }

                showLoading = false;
                graph.getLegendRenderer().resetStyles();
            }
        }).start();
    }

    private void plotFuelLevel() {
        getDataOfLogDouble(imcMessageName);
    }

    private void plotCpuUsage() {
        graph.setTitle(imcMessageName);
        double value[] = new double[3200000];
        int cnt = 0;

        for (IMCMessage m : m_index.getIterator(imcMessageName)) {
            if(m_index.getEntityName(m.getSrcEnt()).equals("Daemon"))
                value[cnt++] = m.getInteger("value");
        }

        DataPoint[] dataPoints0 = new DataPoint[cnt];
        for (int t = 0; t < cnt; t++)
            dataPoints0[t] = new DataPoint(t, value[t]);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints0);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(cnt);
        series.setTitle("Daemon");
        series.setColor(Color.BLUE);
        graph.addSeries(series);
    }

    private void plotEulerAngles() {
        graph.setTitle(imcMessageName);
        double value[][] = new double[4][3200000];
        int cnt[] = new int[4];
        for(int i = 0; i < 4; i++)
            cnt[i] = 0;

        for (IMCMessage m : m_index.getIterator(imcMessageName)) {
            value[0][cnt[0]++] = m.getDouble("phi");
            value[1][cnt[1]++] = m.getDouble("theta");
            value[2][cnt[2]++] = m.getDouble("psi");
            value[3][cnt[3]++] = m.getDouble("psi_magnetic");
        }

        DataPoint[][] dataPoints0 = new DataPoint[4][cnt[0]];

        for(int i = 0; i < 4; i++){
            for (int t = 0; t < cnt[i]; t++)
                dataPoints0[i][t] = new DataPoint(t, value[i][t]);
        }

        LineGraphSeries<DataPoint> series[] = new LineGraphSeries[4];
        for(int i = 0; i < 4; i++)
            series[i] = new LineGraphSeries<DataPoint>(dataPoints0[i]);

        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(cnt[0]);
        series[0].setTitle("phi");
        series[1].setTitle("theta");
        series[0].setColor(Color.BLUE);
        series[1].setColor(Color.GREEN);
        graph.addSeries(series[0]);
        graph.addSeries(series[1]);
    }

    private void getEntityInfo(String msg) {
        for (IMCMessage m : m_index.getIterator(msg)) {
            entityLabel[m.getInteger("id")] = m.getString("label");
        }
    }

    private void plotRpm() {
        getDataOfLogDouble(imcMessageName);
    }

    private void plotCurrent() {
        getDataOfLogDouble(imcMessageName);
    }

    private void plotVoltage(){
        getDataOfLogDouble(imcMessageName);
    }

    private void plotTemperature(){
        getDataOfLogDouble(imcMessageName);
    }

    private void plotSetThrusterActuation() {
        double value[][] = new double[2][3200000];
        boolean haveTwoMotors = false;
        int cnt0 = 0;
        int cnt1 = 0;
        graph.setTitle(imcMessageName);
        for (IMCMessage m : m_index.getIterator(imcMessageName)) {
            if(m.getInteger("id") != 0){
                haveTwoMotors = true;
                break;
            }
        }
        for (IMCMessage m : m_index.getIterator(imcMessageName)) {
            if(m.getInteger("id") == 0)
                value[0][cnt0++] = m.getDouble("value");
            else if(m.getInteger("id") == 1)
                value[1][cnt1++] = m.getDouble("value");
        }

        DataPoint[] dataPoints0 = new DataPoint[cnt0];
        for (int i = 0; i < cnt0; i++)
            dataPoints0[i] = new DataPoint(i, value[0][i]);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints0);
        graph.getViewport().setMinY(-1.2);
        graph.getViewport().setMaxY(1.2);
        graph.getViewport().setMinX(0);
        if(cnt0 >= cnt1)
            graph.getViewport().setMaxX(cnt0);
        else
            graph.getViewport().setMaxX(cnt1);

        series.setTitle("Id 0");
        series.setColor(Color.GREEN);
        graph.addSeries(series);

        if(haveTwoMotors) {
            DataPoint[] dataPoints1 = new DataPoint[cnt1];
            for (int i = 0; i < cnt1; i++)
                dataPoints1[i] = new DataPoint(i, value[1][i]);

            LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(dataPoints1);
            series2.setTitle("Id 1");
            series2.setColor(Color.BLUE);
            graph.addSeries(series2);
        }
    }

    //###########################################################################################

    private DataPoint[] getDataPoint(int cnt_values, int id, double indexEntity[][]){
        DataPoint[] dataPoints = new DataPoint[cnt_values];
        for(int i = 0; i < cnt_values ; i++) {
            dataPoints[i] = new DataPoint(i, indexEntity[id][i]);
        }
        return dataPoints;
    }

    private void getDataOfLogDouble(String imcMessage){
        final String indexName[] = new String[24];
        int cnt = 0;
        for (IMCMessage m : m_index.getIterator(imcMessage)) {
            if(cnt == 0){
                indexName[cnt++] = m_index.getEntityName(m.getSrcEnt());
            }
            else {
                boolean haveIndex = false;
                for (int i = 0; i < cnt; i++) {
                    if (indexName[i].equals(m_index.getEntityName(m.getSrcEnt()))) {
                        haveIndex = true;
                    }
                }
                if(!haveIndex)
                    indexName[cnt++] = m_index.getEntityName(m.getSrcEnt());
            }
        }

        double indexEntity[][] = new double[cnt][3200000];
        int cntIndex[] = new int[cnt];
        for(int i = 0; i < cnt; i++)
            cntIndex[i] = 0;

        for (IMCMessage m : m_index.getIterator(imcMessage)) {
            for(int t = 0; t < cnt; t++) {
                if (m_index.getEntityName(m.getSrcEnt()).equals(indexName[t])) {
                    indexEntity[t][cntIndex[t]++] = m.getDouble("value");
                }
            }
        }

        plotData(cntIndex, cnt, indexEntity, indexName);
    }

    private void getDataOfLogInt(String imcMessage){
        final String indexName[] = new String[256];
        int cnt = 0;
        for (IMCMessage m : m_index.getIterator(imcMessage)) {
            if(cnt == 0){
                indexName[cnt++] = m_index.getEntityName(m.getSrcEnt());
            }
            else {
                boolean haveIndex = false;
                for (int i = 0; i < cnt; i++) {
                    if (indexName[i].equals(m_index.getEntityName(m.getSrcEnt()))) {
                        //Log.i(TAG, m_index.getEntityName(m.getSrcEnt()));
                        haveIndex = true;
                    }
                }
                if(!haveIndex) {
                    Log.i(TAG, m_index.getEntityName(m.getSrcEnt()));
                    indexName[cnt++] = m_index.getEntityName(m.getSrcEnt());
                }
            }
        }

        double indexEntity[][] = new double[cnt][3200000];
        int cntIndex[] = new int[cnt];
        for(int i = 0; i < cnt; i++)
            cntIndex[i] = 0;

        for (IMCMessage m : m_index.getIterator(imcMessage)) {
            for(int t = 0; t < cnt; t++) {
                if (m_index.getEntityName(m.getSrcEnt()).equals(indexName[t])) {
                    indexEntity[t][cntIndex[t]++] = m.getInteger("value");
                }
            }
        }

        plotData(cntIndex, cnt, indexEntity, indexName);
    }

    private void plotData(int cntIndex[], int cnt, double indexEntity[][], final String indexName[]){
        int max_x = 0;
        for(int i = 0; i < cnt; i++) {
            if(cntIndex[i] > max_x)
                max_x = cntIndex[i];
        }

        int min_x = 0;
        for(int i = 0; i < cnt; i++) {
            if(cntIndex[i] < min_x)
                min_x = cntIndex[i];
        }

        double max_y = 0;
        for(int t = 0; t < cnt; t++) {
            for (int i = 0; i < cntIndex[t]; i++) {
                if (indexEntity[t][i] > max_y)
                    max_y = indexEntity[t][i];
            }
        }

        double min_y = 0;
        for(int t = 0; t < cnt; t++) {
            for (int i = 0; i < cntIndex[t]; i++) {
                if (indexEntity[t][i] < min_y)
                    min_y = indexEntity[t][i];
            }
        }

        final LineGraphSeries<DataPoint> series[] = new LineGraphSeries[cnt];
        for(int i = 0; i < cnt; i++) {
            series[i] = new LineGraphSeries<DataPoint>(getDataPoint(cntIndex[i], i, indexEntity));
            series[i].setTitle(indexName[i]);
            series[i].setColor(color[i]);
            graph.addSeries(series[i]);
        }

        graph.setTitle(imcMessageName);
        graph.getViewport().setMinY(min_y - 2);
        graph.getViewport().setMaxY(max_y+2);
        graph.getViewport().setMinX(min_x - 2);
        graph.getViewport().setMaxX(max_x+2);
        //graph.getViewport().setXAxisBoundsManual(false);

        if(cnt > 1)
            showButton = true;
        else
            showButton = false;

        final int finalCnt = cnt;
        final boolean[] Selectedtruefalse = new boolean[finalCnt];
        for(int i = 0; i < finalCnt; i++)
            Selectedtruefalse[i] = true;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertdialogbuilder;
                alertdialogbuilder = new AlertDialog.Builder(DisplayGraph.this);
                alertdialogbuilder.setMultiChoiceItems(Arrays.copyOf(indexName, finalCnt), Selectedtruefalse, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    }
                });
                alertdialogbuilder.setCancelable(false);
                alertdialogbuilder.setTitle("Select Series Here");
                alertdialogbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int a = 0;
                        graph.removeAllSeries();
                        while(a < Selectedtruefalse.length)
                        {
                            boolean value = Selectedtruefalse[a];
                            if(value) {
                                graph.addSeries(series[a]);
                            }
                            else {
                                graph.removeSeries(series[a]);
                            }
                            a++;
                        }
                    }
                });

                alertdialogbuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = alertdialogbuilder.create();
                dialog.show();
            }
        });
    }
}
