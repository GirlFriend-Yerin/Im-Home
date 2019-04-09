package sysproj.seonjoon.iot_personal_projectapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageShowActivity extends Activity{

    private final String urlHead = "http://ec2-52-34-74-63.us-west-2.compute.amazonaws.com:8080/API/PictureView";

    private String machine_id ;
    private String fileName ;
    private ImageView imageView;
    private Context mContext;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);

        Intent intent = getIntent();
        machine_id = intent.getStringExtra("ID");
        fileName = intent.getStringExtra("file");

        imageView = (ImageView) findViewById(R.id.image_show_view);

        LoadImageAsync loadImageAsync = new LoadImageAsync();
        loadImageAsync.execute();
    }

    private class LoadImageAsync extends AsyncTask<Void, Void, Void> {
        private final String ID_TAG = "machine_id";
        private final String FILE_TAG = "file";
        private final String KIND_TAG = "tag";

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(urlHead);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setConnectTimeout(3000);
                connection.setDoOutput(true);
                connection.setDoInput(true);

                // Make Parameter
                StringBuffer buffer = new StringBuffer();
                buffer.append(ID_TAG).append("=").append(machine_id).append("&");
                buffer.append(FILE_TAG).append("=").append(fileName).append("&");
                buffer.append(KIND_TAG).append("=").append("I");

                Log.e("data", buffer.toString());

                // Send Packet
                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "euc-kr");
                PrintWriter pw = new PrintWriter(osw);
                pw.write(buffer.toString());
                pw.flush();
                pw.close();

                int responseCode = connection.getResponseCode();

                Log.e("Check", "Res " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    image = BitmapFactory.decodeStream(is);
                }
            } catch (MalformedURLException e) {
                Log.e("ImageThread", "MalformedURL Exception");
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "URL 형식이 올바르지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ImageThread", "IO Exception");
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "정보를 가져오는도중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("TempThread", "Interrupted Exception");
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            imageView.setImageBitmap(image);
        }
    }
}
