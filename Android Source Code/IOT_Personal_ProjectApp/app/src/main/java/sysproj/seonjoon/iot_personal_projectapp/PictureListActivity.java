package sysproj.seonjoon.iot_personal_projectapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class PictureListActivity extends Activity {

    private final String urlHead = "http://ec2-52-34-74-63.us-west-2.compute.amazonaws.com:8080/API/PictureList";
    private String machine_id = null;
    private Context mContext;
    private ArrayList<String> fileNameList = new ArrayList<>();
    private PictureListAdapter listAdapter;

    private ListView itemListView;
    private ImageButton beforeButton;
    private ImageButton nextButton;
    private TextView pageLabel;
    private LoadListAsync loadListAsync = null;

    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_list);

        Intent intent = getIntent();
        machine_id = intent.getStringExtra("ID");
        mContext = this;

        itemListView = findViewById(R.id.picture_list_view);
        beforeButton = findViewById(R.id.page_before);
        nextButton = findViewById(R.id.page_next);
        pageLabel = findViewById(R.id.page_current_label);
        listAdapter = new PictureListAdapter(this, fileNameList);

        itemListView.setAdapter(listAdapter);

        loadListAsync = new LoadListAsync();
        loadListAsync.execute();
        pageLabel.setText(Integer.toString(currentPage));

        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(PictureListActivity.this, ImageShowActivity.class);
                intent.putExtra("ID", machine_id);
                intent.putExtra("file", fileNameList.get(i));
                startActivity(intent);
            }
        });

        beforeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage -= 1;
                pageLabel.setText(Integer.toString(currentPage));
                loadListAsync = new LoadListAsync();
                loadListAsync.execute();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage += 1;
                pageLabel.setText(Integer.toString(currentPage));
                loadListAsync = new LoadListAsync();
                loadListAsync.execute();
            }
        });
    }

    private class LoadListAsync extends AsyncTask<Void, Void, Void> {
        private final String ID_TAG = "machine_id";
        private final String PAGE_TAG = "page";

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
                buffer.append(PAGE_TAG).append("=").append("0");

                // Send Packet
                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "euc-kr");
                PrintWriter pw = new PrintWriter(osw);
                pw.write(buffer.toString());
                pw.flush();
                pw.close();

                int responseCode = connection.getResponseCode();

                Log.e("Check", "Res " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = br.readLine();
                    JSONObject result = new JSONObject(line);

                    boolean isExist = result.getBoolean("exist");
                    if (isExist)
                    {
                        JSONArray fileList = result.getJSONArray("List");
                        Log.e("Length", fileList.length() + " Exist");
                        for (int i = 0 ; i < fileList.length(); i++)
                            fileNameList.add(fileList.getString(i));
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("ListThread", "MalformedURL Exception");
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "URL 형식이 올바르지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TempThread", "IO Exception");
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "정보를 가져오는도중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (JSONException e) {
                Log.e("TempThread", "JSON Exception");
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "정보를 읽는 도중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
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
        protected void onPreExecute() {
            super.onPreExecute();
            beforeButton.setEnabled(false);
            nextButton.setEnabled(false);
            fileNameList.clear();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (fileNameList.isEmpty())
            {
                nextButton.setEnabled(false);
                if (currentPage > 1)
                    currentPage--;
            }
            else if (fileNameList.size() < 30)
            {
                nextButton.setEnabled(false);
                listAdapter.notifyDataSetChanged();
            }
            else
            {
                nextButton.setEnabled(true);
                listAdapter.notifyDataSetChanged();
            }
            pageLabel.setText(Integer.toString(currentPage));

        }
    }
}

