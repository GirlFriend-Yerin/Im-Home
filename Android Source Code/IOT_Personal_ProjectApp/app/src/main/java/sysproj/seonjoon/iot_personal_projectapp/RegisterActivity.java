package sysproj.seonjoon.iot_personal_projectapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private Context context;
    private EditText idEdit;
    private EditText passwordEdit;
    private EditText machine_idEdit;
    private Button checkDuplicateButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        context = this;
        idEdit = (EditText) findViewById(R.id.register_id_edittext);
        passwordEdit = (EditText) findViewById(R.id.register_password_text);
        machine_idEdit = (EditText) findViewById(R.id.register_machine_id_edittext);
        checkDuplicateButton = (Button) findViewById(R.id.register_duplicate_button);
        registerButton = (Button) findViewById(R.id.register_register_button);

        checkDuplicateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckDuplicateAsync duplicateAsync = new CheckDuplicateAsync();
                duplicateAsync.execute(idEdit.getText().toString());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterAsync registerAsync = new RegisterAsync();
                registerAsync.execute(idEdit.getText().toString(), passwordEdit.getText().toString(), machine_idEdit.getText().toString());
            }
        });
    }

    private class CheckDuplicateAsync extends AsyncTask<String, Void, Boolean> {
        private final String urlHead = "http://ec2-52-34-74-63.us-west-2.compute.amazonaws.com:8080/API/CheckDuplicate";
        private final String ID_TAG = "id";

        @Override
        protected Boolean doInBackground(String... strings) {
            String id = strings[0];
            boolean result = true;

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
                buffer.append(ID_TAG).append("=").append(id);

                // Send Packet
                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "euc-kr");
                PrintWriter pw = new PrintWriter(osw);
                pw.write(buffer.toString());
                pw.flush();
                pw.close();

                int responseCode = connection.getResponseCode();

                Log.e("Login", "Res " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = br.readLine().replaceAll(" ","");
                    result = line.contains("true");
                }
            } catch (MalformedURLException e) {
                Log.e("DupleThread", "MalformedURL Exception");
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "URL 형식이 올바르지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("DupleThread", "IO Exception");
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "정보를 가져오는도중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("DupleThread", "Interrupted Exception");
                }

                return result;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            final boolean dupleResult = result;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dupleResult)
                        Toast.makeText(context, "중복된 ID 입니다.", Toast.LENGTH_SHORT).show();
                    else {
                        idEdit.setEnabled(false);
                        passwordEdit.setEnabled(true);
                        machine_idEdit.setEnabled(true);
                    }
                }
            });
        }
    }

    private class RegisterAsync extends AsyncTask<String, Void, Boolean> {
        private final String urlHead = "http://ec2-52-34-74-63.us-west-2.compute.amazonaws.com:8080/API/Register";
        private final String ID_TAG = "id";
        private final String PW_TAG = "password";
        private final String MACHINE_TAG = "machine_id";

        @Override
        protected Boolean doInBackground(String... strings) {
            String id = strings[0];
            String password = strings[1];
            String machine_id = strings[2];
            boolean result = false;

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
                buffer.append(ID_TAG).append("=").append(id).append("&");
                buffer.append(PW_TAG).append("=").append(password).append("&");
                buffer.append(MACHINE_TAG).append("=").append(machine_id);

                // Send Packet
                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "euc-kr");
                PrintWriter pw = new PrintWriter(osw);
                pw.write(buffer.toString());
                pw.flush();
                pw.close();

                Log.e("BUF", buffer.toString());

                int responseCode = connection.getResponseCode();

                Log.e("Login", "Res " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = br.readLine();
                    Log.e("Register", line);
                    if (line.contains("Success"))
                        result = true;
                    else
                        result = false;
                    Log.e("Register", Boolean.toString(result));
                }
            } catch (MalformedURLException e) {
                Log.e("ListThread", "MalformedURL Exception");
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "URL 형식이 올바르지 않습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("RegisterThread", "IO Exception");
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "정보를 가져오는도중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("RegisterThread", "Interrupted Exception");
                }

                return result;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "생성되었습니다.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "ID, Password는 대소문자, 숫자, _ 로만 구성 가능합니다.", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    }
}
