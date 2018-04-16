package com.example.errortime.kati_command;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SettingActivity extends AppCompatActivity {
    private EditText token_text;
    private EditText ipaddress_text;
    private SharedPreferences sp;
    private Button submit_button;
    private TextInputLayout ipaddress_inputlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        ipaddress_text = (EditText) findViewById(R.id.ipaddress_text);
        ipaddress_inputlayout = (TextInputLayout) findViewById(R.id.ipaddress_inputlayout);
        token_text = (EditText) findViewById(R.id.token_text);
        set_token_text();
        Button copy_token__button = (Button) findViewById(R.id.copy_token_button);
        copy_token__button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(token_text.getText());
                Toast.makeText(getApplicationContext(),
                        R.string.warning_copy_name, Toast.LENGTH_LONG).show();
            }
        });
        sp = getSharedPreferences("PREF_SETTING", Context.MODE_PRIVATE);
        String default_ip = sp.getString("setting_ip_address", "127.0.0.1");
        ipaddress_text.setText(default_ip);
        submit_button = (Button) findViewById(R.id.submit_button);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(is_ip_v4(ipaddress_text.getText().toString())){
                    ipaddress_inputlayout.setError(null);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("setting_ip_address", String.valueOf(ipaddress_text.getText()));
                    editor.apply();
                    String default_ip = sp.getString("setting_ip_address", "127.0.0.1");
                    ipaddress_text.setText(default_ip);
                    Toast.makeText(getApplicationContext(),
                            R.string.warning_saved_success_name, Toast.LENGTH_LONG).show();
                }
                else{
                    ipaddress_inputlayout.setError(getString(R.string.ip_address_format_error));
                }
            }
        });
    }

    private void set_token_text(){
        String token = FirebaseInstanceId.getInstance().getToken();
        token_text.setText(token);
        Log.d("myTag", token);

    }

    private static boolean is_ip_v4(String ipaddress) {
        if (ipaddress == null) {
            return false;
        }
        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipaddress);
        return matcher.matches();
    }

}
