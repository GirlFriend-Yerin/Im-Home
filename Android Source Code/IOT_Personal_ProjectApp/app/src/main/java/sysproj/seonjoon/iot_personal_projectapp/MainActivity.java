package sysproj.seonjoon.iot_personal_projectapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private final String urlHead = "http://ec2-52-34-74-63.us-west-2.compute.amazonaws.com:8080/API/HNT?";
    private String machine_id;

    private Context mContext;
    private String clientName;
    private TextView clientNameText;
    private TextView temperatureText;
    private TextView humidityText;
    private Button videoShowButton;
    private Button snapshotButton;
    private boolean isNeedUpdate = true;
    private Thread humidityThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        Intent intent = getIntent();
        machine_id = intent.getStringExtra("machine_id");
        clientNameText = (TextView) findViewById(R.id.text_client_name);
        temperatureText = (TextView) findViewById(R.id.text_temperature);
        humidityText = (TextView) findViewById(R.id.text_humidity);
        videoShowButton = (Button) findViewById(R.id.button_video);
        snapshotButton = (Button) findViewById(R.id.button_snapshot);

        clientNameText.setText(clientName);

        setListener();

        humidityThread = new Thread(new HumidityThread());
        humidityThread.setDaemon(true);
        humidityThread.start();

//        loadTempHumiAsync HnT = new loadTempHumiAsync();
//        HnT.execute(machine_id);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setListener() {
        snapshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isNeedUpdate = false;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, PictureListActivity.class);
                        intent.putExtra("ID", machine_id);
                        startActivity(intent);
                        // startActivityForResult(intent, RESPONSE_CODE);
                    }
                }, 800);
            }
        });
    }

    private class HumidityThread implements Runnable {

        private final String ID_TAG = "machine_id=";
        private double temperature = 0;
        private double humidity = 0;

        @Override
        public void run() {
            Log.e("Humi", "Start");
            while (true) {
                try {
                    URL url = new URL(urlHead + ID_TAG + machine_id);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.addRequestProperty(ID_TAG, machine_id);
                    connection.setConnectTimeout(2000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line = null;

                        while ((line = br.readLine()) != null) {
                            JSONObject result = new JSONObject(line);
                            temperature = result.getDouble("temperature");
                            humidity = result.getDouble("humidity");
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                temperatureText.setText("Temperature : " + temperature);
                                humidityText.setText("Humidity : " + humidity);

                                if (temperature > 20)
                                    emergencyNofification(temperature);
                            }
                        });

                        br.close();
                        connection.disconnect();
                    }
                } catch (MalformedURLException e) {
                    Log.e("TempThread", "MalformedURL Exception");
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "URL 형식이 올바르지 않습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("TempThread", "IO Exception");
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "정보를 가져오는도중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                } catch (JSONException e) {
                    Log.e("TempThread", "JSON Exception");
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "정보를 읽는 도중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                } finally {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e("TempThread", "Interrupted Exception");
                        break;
                    }
                }
            }
            isNeedUpdate = true;
        }
    }

    private void emergencyNofification(double temperature) {
        Resources resources = getResources();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle("화재 감지 경보")
                .setContentTitle("온도가 " + temperature + "도 입니다.")
                .setTicker("화재 의심 감지")
                .setSmallIcon(R.drawable.ic_home_logo)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(5102, builder.build());
    }
}
