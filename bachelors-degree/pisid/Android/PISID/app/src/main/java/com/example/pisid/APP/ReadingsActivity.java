package com.example.pisid.APP;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.pisid.APP.Connection.ConnectionHandler;
import com.example.pisid.APP.Database.DatabaseHandler;
import com.example.pisid.APP.Database.DatabaseReader;
import com.example.pisid.APP.Helper.UserLogin;
import com.example.pisid.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

public class ReadingsActivity extends AppCompatActivity {

    private static final String IP = UserLogin.getInstance().getIp();
    private static final String PORT = UserLogin.getInstance().getPort();
    private static final String username= UserLogin.getInstance().getUsername();
    private static final String password = UserLogin.getInstance().getPassword();

    String getMedicoes = "http://" + IP + ":" + PORT + "/scripts/getReadings.php";
    DatabaseHandler db = new DatabaseHandler(this);

    Handler h = new Handler(); 
    int delay = 1000; //1 second=1000 milisecond
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicoes);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        updateReadings();
        drawGraphs();
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible
        h.postDelayed( runnable = new Runnable() {
            public void run() {
                updateReadings();
                drawGraphs();

                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    public void alerts(View v){
        Intent i = new Intent(this, AlertsActivity.class);
        startActivity(i);
    }

    private void updateReadings(){
        db.clearReadings();
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        ConnectionHandler jParser = new ConnectionHandler();
        JSONArray medicoes = jParser.getJSONFromUrl(getMedicoes, params);
        try {
            if (medicoes != null){
                for (int i=0;i< medicoes.length();i++){
                    JSONObject c = medicoes.getJSONObject(i);
                    String hora = c.getString("Hora");
                    double leitura;
                    try {
                        leitura = c.getDouble("Leitura");
                    } catch (Exception e) {
                        leitura = -1000.0;
                    }
                    db.insertReading(hora, leitura);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawGraphs(){
        GraphView graphTemperatura = findViewById(R.id.temperatura_graph);
        graphTemperatura.removeAllSeries();
        int helper=0;
        DatabaseReader dbReader = new DatabaseReader(db);
        Cursor cursorTemperatura = dbReader.readReadings();
        Date currentTimestamp = new Date();
        long currentLong = currentTimestamp.getTime();

        DataPoint[] datapointsTemperatura = new DataPoint[cursorTemperatura.getCount()];

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (cursorTemperatura.moveToNext()){
            String hora =  cursorTemperatura.getString(cursorTemperatura.getColumnIndex("Hora"));
            Double valorMedicao = cursorTemperatura.getDouble(cursorTemperatura.getColumnIndex("Leitura"));
            try {
                Date date = format.parse(hora);
                long pointLong = date.getTime();
                long difference = currentLong - pointLong;
                double seconds = TimeUnit.MILLISECONDS.toSeconds(difference);
                datapointsTemperatura[helper]=new DataPoint(seconds,valorMedicao);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            helper++;
        }
        DataPoint[] datapoints = new DataPoint[datapointsTemperatura.length];
        int j=0;
        for (int i=datapointsTemperatura.length-1; i>=0; i--) {
            datapoints[j] = datapointsTemperatura[i];
            j++;
        }

        cursorTemperatura.close();

        graphTemperatura.getViewport().setXAxisBoundsManual(true);
        graphTemperatura.getViewport().setMinX(0);
        graphTemperatura.getViewport().setMaxX(180);
        LineGraphSeries<DataPoint> seriesTemperatura = new LineGraphSeries<>(datapoints);
        seriesTemperatura.setColor(Color.RED);
        seriesTemperatura.setTitle("Temperatura");
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphTemperatura);
        graphTemperatura.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graphTemperatura.getLegendRenderer().setVisible(true);
        graphTemperatura.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphTemperatura.getLegendRenderer().setBackgroundColor(Color.alpha(0));
        graphTemperatura.addSeries(seriesTemperatura);
    }

}
