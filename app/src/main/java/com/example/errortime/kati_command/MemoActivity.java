package com.example.errortime.kati_command;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Error Time on 1/26/2018.
 */

public class MemoActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName(),default_ip;;
    private SharedPreferences sp;
    private TextInputLayout date_inputlayout,time_inputlayout;
    private ImageButton mic_button;
    private EditText memo_desc_edittext,date_edittext;
    private Spinner hours_spinner,minutes_spinner;
    private Calendar myCalendar = Calendar.getInstance();
    private Button submit_button;
    private final static int REQUEST_VOICE_RECOGNITION = 10001;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        sp = getSharedPreferences("PREF_SETTING", Context.MODE_PRIVATE);
        default_ip = sp.getString("setting_ip_address", "127.0.0.1");
        Intent intent = getIntent();
        date_inputlayout = (TextInputLayout) findViewById(R.id.date_inputlayout);
        time_inputlayout = (TextInputLayout) findViewById(R.id.time_inputlayout);
        memo_desc_edittext = (EditText) findViewById(R.id.memo_desc_edittext);
        mic_button = (ImageButton) findViewById(R.id.mic_button);
        date_edittext = (EditText) findViewById(R.id.date_edittext);
        hours_spinner = (Spinner) findViewById(R.id.hours_spinner);
        minutes_spinner = (Spinner) findViewById(R.id.minutes_spinner);
        submit_button = (Button) findViewById(R.id.submit_button);
        memo_desc_edittext.setText(intent.getStringExtra("memo_desc_text"));
        final DatePickerDialog.OnDateSetListener datepicker = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                set_date();
                get_hours(String.valueOf(date_edittext.getText()));
            }

        };
        date_edittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MemoActivity.this, (DatePickerDialog.OnDateSetListener) datepicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        hours_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String values = ((Spinner)findViewById(R.id.hours_spinner)).getSelectedItem().toString();
                get_minutes(values,String.valueOf(date_edittext.getText()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        mic_button.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View view) {
              callVoiceRecognition();
          }
        });
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String memodesc = memo_desc_edittext.getText().toString();
                String memonotificationdate = date_edittext.getText().toString();
                if(memodesc.equals("")){
                    /*memo_desc_inputlayout.setError(getString(R.string.memo_desc_is_blank));*/
                }
                if(memonotificationdate.toString().equals("")){
                    date_inputlayout.setError(getString(R.string.date_is_blank));
                }
                if (hours_spinner.getCount()==0 || minutes_spinner.getCount()==0){
                    time_inputlayout.setError(getString(R.string.time_is_blank));
                }
                if(!memodesc.equals("") && !memonotificationdate.equals("") && hours_spinner.getCount() > 0 && minutes_spinner.getCount() > 0){
                    String memonotificationtime = ((Spinner)findViewById(R.id.hours_spinner)).getSelectedItem().toString()+":"+((Spinner)findViewById(R.id.minutes_spinner)).getSelectedItem().toString()+":"+"00";
                    insert_memo(memodesc,memonotificationdate,memonotificationtime);
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        default_ip = sp.getString("setting_ip_address", "127.0.0.1");
    }

    private void callVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, REQUEST_VOICE_RECOGNITION);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VOICE_RECOGNITION &&
            resultCode == RESULT_OK &&
            data != null) {
            ArrayList<String> resultList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = resultList.get(0);
            memo_desc_edittext.setText(text);
        }
    }

    private void set_date() {
        String myFormat1 = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat1, Locale.US);
        date_edittext.setText(sdf.format(myCalendar.getTime()));
    }

    private ArrayList<String> jsonStringToArray(String jsonString) throws JSONException {
        ArrayList<String> stringArray = new ArrayList<String>();
        JSONArray jsonArray = new JSONArray(jsonString);
        for (int i = 0; i < jsonArray.length(); i++) {
            stringArray.add(jsonArray.getString(i));
        }
        return stringArray;
    }

    private void set_hours(String json){
        ArrayAdapter<String> adapterThai = null;
        try {
            adapterThai = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, jsonStringToArray(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hours_spinner.setAdapter(adapterThai);
    }
    private void set_minutes(String json){
        ArrayAdapter<String> adapterThai = null;
        try {
            adapterThai = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, jsonStringToArray(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        minutes_spinner.setAdapter(adapterThai);
    }
    private void get_hours(final String dayofweek) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient okHttpClient = new OkHttpClient();

                String url = "http://"+default_ip+"/kati/api/select_time_picker_hours_memo_by_day_json.php";
                Log.e(TAG, url);
                RequestBody body = new FormBody.Builder()
                        .add("dayofweek", dayofweek)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        return response.body().string();
                    } else {
                        return "Error";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error";
                }
            }

            @Override
            protected void onPostExecute(String string) {
                super.onPostExecute(string);
                if(string.equals("Error")){
                    hours_spinner.setAdapter(null);
                    minutes_spinner.setAdapter(null);
                    time_inputlayout.setError(getString(R.string.cant_connect_server));
                }
                else{
                    set_hours(string);
                    time_inputlayout.setError(null);
                }
            }
        }.execute();
    }

    private void get_minutes(final String hours,final String dayofweek) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient okHttpClient = new OkHttpClient();

                String url = "http://"+default_ip+"/kati/api/select_time_picker_minutes_memo_by_day_json.php";
                Log.e(TAG, url);
                RequestBody body = new FormBody.Builder()
                        .add("hours", hours)
                        .add("dayofweek", dayofweek)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        return response.body().string();
                    } else {
                        return "Error";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error";
                }
            }

            @Override
            protected void onPostExecute(String string) {
                super.onPostExecute(string);
                if(string.equals("Error")){
                    hours_spinner.setAdapter(null);
                    minutes_spinner.setAdapter(null);
                    time_inputlayout.setError(getString(R.string.cant_connect_server));
                }
                else {
                    set_minutes(string);
                    time_inputlayout.setError(null);
                }
            }
        }.execute();
    }

    private void insert_memo(final String memodesc,final String memonotificationdate,final String memonotificationtime) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient okHttpClient = new OkHttpClient();

                String url = "http://"+default_ip+"/kati/api/insert_memo_by_day.php";
                Log.e(TAG, url);
                RequestBody body = new FormBody.Builder()
                        .add("Memo_desc", memodesc)
                        .add("Memo_notification_date", memonotificationdate)
                        .add("Memo_notification_time", memonotificationtime)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        return response.body().string();
                    } else {
                        return "Error";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error";
                }
            }

            @Override
            protected void onPostExecute(String string) {
                super.onPostExecute(string);
                if(string.equals("Error")){
                    Toast.makeText(MemoActivity.this, getString(R.string.warning_send_http_unsuccess),
                            Toast.LENGTH_LONG).show();
                }
                else {
                    finish();
                }
            }
        }.execute();
    }
}

