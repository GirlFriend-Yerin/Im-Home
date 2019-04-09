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
import android.widget.ImageView;
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

public class LoginActivity extends Activity {

    private Context context;

    private EditText idEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        idEditText = (EditText) findViewById(R.id.login_id_edit);
        passwordEditText = (EditText) findViewById(R.id.login_password_edit);
        loginButton = (Button) findViewById(R.id.login_login_button);
        registerButton = (Button) findViewById(R.id.login_register_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String id = idEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (id.contains(" ") || password.contains(" ")) {
                    Toast.makeText(context, "ID, Password에는 공백이 포함될수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    LoginAsync loginAsync = new LoginAsync();
                    loginAsync.execute(id, password);
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private class LoginAsync extends AsyncTask<String, Void, String> {
        private final String urlHead = "http://ec2-52-34-74-63.us-west-2.compute.amazonaws.com:8080/API/Login";
        private final String ID_TAG = "id";
        private final String PW_TAG = "password";
        private final int NOT_FOUND = 404;
        private final int NOT_MATCH = 500;

        @Override
        protected String doInBackground(String... strings) {
            String id = strings[0];
            String password = strings[1];
            String result = "";

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
                buffer.append(PW_TAG).append("=").append(password);

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
                    result = br.readLine();
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
                Log.e("TempThread", "IO Exception");
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
                    Log.e("TempThread", "Interrupted Exception");
                }

                return result;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.isEmpty())
                return ;

            try {
                final int code = Integer.parseInt(s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ( code == NOT_FOUND ){
                            Toast.makeText(context, "존재하지 않는 아이디입니댜.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(context, "비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (NumberFormatException e) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("machine_id", s.replaceAll(" ", ""));
                startActivity(intent);
                finish();
            }
        }
    }
}

