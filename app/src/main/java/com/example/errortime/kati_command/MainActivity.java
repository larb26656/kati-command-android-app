package com.example.errortime.kati_command;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ListView listView;
    private List<ChatMessage> chatMessages;
    private ArrayAdapter<ChatMessage> adapter;
    private ImageButton mic_button;
    private TextView process_textview;
    private SharedPreferences sp;
    private EditText text_input;
    private String default_ip;
    private  String texts;
    private final static int REQUEST_VOICE_RECOGNITION = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        sp = getSharedPreferences("PREF_SETTING", Context.MODE_PRIVATE);
        default_ip = sp.getString("setting_ip_address", "127.0.0.1");

        mic_button = (ImageButton) findViewById(R.id.mic_button);
        process_textview = (TextView) findViewById(R.id.process_textview);
        text_input = (EditText) findViewById(R.id.editText);
        hide_process_text();
        chatMessages = new ArrayList<>();

        listView = (ListView) findViewById(R.id.list_msg);
        //set ListView adapter first
        adapter = new MessageAdapter(this, R.layout.item_chat_left, chatMessages);
        listView.setAdapter(adapter);
    }
    public void show_process_text(){
        process_textview.setVisibility(View.VISIBLE);
        mic_button.setVisibility(View.INVISIBLE);
    }
    public void hide_process_text(){
        process_textview.setVisibility(View.INVISIBLE);
        mic_button.setVisibility(View.VISIBLE);
    }
    public void input_chat(String text){
        boolean isMine = false;
        ChatMessage chatMessage = new ChatMessage(text, isMine);
        chatMessages.add(chatMessage);
        adapter.notifyDataSetChanged();
    }
    public void respon_chat(String text){
        boolean isMine = true;
        ChatMessage chatMessage = new ChatMessage(text, isMine);
        chatMessages.add(chatMessage);
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onResume(){
        super.onResume();
        default_ip = sp.getString("setting_ip_address", "127.0.0.1");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                open_setting_activity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void open_setting_activity(){
        Intent myIntent = new Intent(MainActivity.this, SettingActivity.class);
        MainActivity.this.startActivity(myIntent);
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
            texts=resultList.get(0);
            show_process_text();
            input_chat(texts);
            //new MainActivity.OkHttpAync().execute(this, "get", "");
            //respon_chat(getString(R.string.warning_process));
        }
    }
    private void sent_command(final String text) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                OkHttpClient okHttpClient = new OkHttpClient();

                String url = "http://"+default_ip+":5000/sent?text="+text_input;
                Log.e(TAG, url);
                RequestBody body = new FormBody.Builder()
                        .add("text", text)
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
                    respon_chat(getString(R.string.warning_send_http_unsuccess));
                }
                else{
                    String json_respon = (String) string;
                    JSONObject jsonobj;
                    try {
                        jsonobj = new JSONObject(json_respon);
                        if(jsonobj.getString("type").equals("memo_save")){
                            respon_chat(jsonobj.getString("text"));
                            Intent myIntent = new Intent(MainActivity.this, MemoActivity.class);
                            myIntent.putExtra("memo_desc_text", jsonobj.getString("text"));
                            MainActivity.this.startActivity(myIntent);
                        }
                        else{
                            respon_chat(jsonobj.getString("text"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }
    public void getHttp(View v) {
        //instantiate async task which call service using OkHttp in the background
        // and execute it passing required parameter to it
    //get http request using okhttp
    //callVoiceRecognition();
    //showChangeLangDialog();
        texts=text_input.getText().toString();
        input_chat(texts);
        text_input.setText("");
        sent_command(texts);


}


  /*  private class OkHttpAync extends AsyncTask<Object, Void, Object> {

        private String TAG = MainActivity.OkHttpAync.class.getSimpleName();
        private Context contx;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object... params) {
            contx = (Context) params[0];
            String requestType = (String) params[1];
            String requestParam = (String) params[2];

            Log.e(TAG, "processing http request in async task");

            if ("get".equals(requestType)) {
                Log.e(TAG, "processing get http request using OkHttp");
                return getHttpResponse(texts);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (result != null) {
                Log.e(TAG, "populate UI after response from service using OkHttp client");
                //respone.setText((String) result);
                String json_respon = (String) result;
                JSONObject jsonobj;
                try {
                    jsonobj = new JSONObject(json_respon);
                    respon_chat(jsonobj.getString("text"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else{
                respon_chat(getString(R.string.warning_send_http_unsuccess));
            }
            hide_process_text();
        }
    }

    public Object getHttpResponse(String text_input) {
        OkHttpClient httpClient = new OkHttpClient();
        //String url = "http://"+default_ip+":5000/sent?text="+text_input;
        String url = "http://"+default_ip+":5000/sent";
        Log.e(TAG, url);
        RequestBody body = new FormBody.Builder()
                .add("text", text_input)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, "error in getting response get request okhttp");
        }
        return null;
    }

    public void showChangeLangDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);

        dialogBuilder.setTitle("Custom dialog");
        dialogBuilder.setMessage("Enter text below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                texts=edt.getText().toString();
                input_chat(texts);
                new MainActivity.OkHttpAync().execute(this, "get", "");
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }*/



}