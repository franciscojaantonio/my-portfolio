package com.example.pisid.APP;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.pisid.APP.Connection.ConnectionHandler;
import com.example.pisid.APP.Helper.UserLogin;
import com.example.pisid.R;

import org.json.JSONArray;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText ip, port, username, password;
    private Button login;
    SharedPreferences myPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        myPrefs = getSharedPreferences("userData", Context.MODE_PRIVATE);

        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        ip.setText(myPrefs.getString("ip",""));
        port.setText(myPrefs.getString("port",""));
        username.setText(myPrefs.getString("username",""));
        password.setText(myPrefs.getString("password",""));

    }

    public void loginClick(View v) {
        String validateLogin = "http://" + ip.getText() + ":" + port.getText() + "/scripts/validateLogin.php";

        HashMap<String, String> params = new HashMap<>();
        params.put("username", username.getText().toString().trim());
        params.put("password", password.getText().toString().trim());
        ConnectionHandler jParser = new ConnectionHandler();
        JSONArray jsonValidacao = jParser.getJSONFromUrl(validateLogin, params);
        if (jsonValidacao != null) {
            SharedPreferences.Editor editor = myPrefs.edit();
            editor.putString("ip", ip.getText().toString().trim());
            editor.putString("port", port.getText().toString().trim());
            editor.putString("username", username.getText().toString().trim());
            editor.putString("password", password.getText().toString().trim());
            editor.apply();

            new UserLogin(ip.getText().toString(), port.getText().toString(), username.getText().toString(), password.getText().toString());

            Intent i = new Intent(this, AlertsActivity.class);
            startActivity(i);
            finish();
        } else {
            new AlertDialog.Builder(v.getContext()).setMessage("Login failed, check if the user exists and the IP is correct").show();
        }
    }
}

