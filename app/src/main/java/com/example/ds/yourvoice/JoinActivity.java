package com.example.ds.yourvoice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by DS on 2018-02-19.
 */

public class JoinActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editTextId;
    private EditText editTextPw;
    private EditText editTextName;
    private Button checkId;
    private String flagId = "idnokay"; //중복체크안한걸로 초기화

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_arrow_left_orange_24);

        editTextId = (EditText)findViewById(R.id.newID);
        editTextPw = (EditText)findViewById(R.id.newPW);
        editTextName = (EditText)findViewById(R.id.newName);
        checkId = findViewById(R.id.checkID);

        editTextId.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(flagId.equals("idokay")) {
                    checkId.setTextColor(Color.WHITE);
                    checkId.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_check_white_24, 0, 0);
                    flagId = "idnokay";
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // custom toast 만들기
    public void makeToast(String s, Drawable d) {
        View view = View.inflate(JoinActivity.this, R.layout.custom_toast, null);
        ImageView iv = view.findViewById(R.id.toast_image);
        iv.setImageDrawable(d);
        TextView tv = view.findViewById(R.id.toast_text);
        tv.setText(s);
        Toast toast = new Toast(JoinActivity.this);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM, 0, 250);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }


    /* 아이디 중복체크버튼 클릭 */
    public void checkID(View v){
        String Id = editTextId.getText().toString();
        if(Id == null || Id.equals("") == true){
            makeToast("아이디를 입력해주세요", getResources().getDrawable(R.drawable.baseline_clear_white_24));
        }else{
            isExistID(Id);
        }
    }

    /* 아이디 중복체크 */
    private boolean isExistID(String Id){
        class checkIdData extends AsyncTask<String, Void, String>{

            @Override
            protected void  onPostExecute(String s) {
                super.onPostExecute(s);

                try {
                    // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                    JSONObject jObject = new JSONObject(s);
                    // results라는 key는 JSON배열로 되어있다.
                    JSONArray results = jObject.getJSONArray("result");
                    String idmsg = "";


                    for ( int i = 0; i < results.length(); ++i ) {
                        JSONObject temp = results.getJSONObject(i);
                        idmsg =temp.get("idmsg").toString();
                        flagId = temp.get("idflag").toString();

                    }
                    //Log.e("ssssssssssssssss",flagId.toString());
                    if(flagId.equals("idokay")) {
                        makeToast(idmsg, getResources().getDrawable(R.drawable.baseline_check_white_24));
                        checkId.setTextColor(Color.BLACK);
                        checkId.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_check_black_24, 0, 0);
                    }
                    else {
                        makeToast(idmsg, getResources().getDrawable(R.drawable.baseline_clear_white_24));
                        checkId.setTextColor(Color.WHITE);
                        checkId.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.baseline_check_white_24, 0, 0);
                    }
                    //Toast.makeText(getApplicationContext(), idmsg, Toast.LENGTH_SHORT). show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(String... params){
                try{
                    String Id = (String) params[0];
                    String CheckId = "ID";
                    String link = "http://13.124.94.107/checkJoinCondition.php";
                    String data = URLEncoder.encode("Id", "UTF-8") + "=" + URLEncoder.encode(Id, "UTF-8");
                    data += "&" + URLEncoder.encode("Check", "UTF-8") + "=" + URLEncoder.encode(CheckId, "UTF-8");
                    data += "&" + URLEncoder.encode("Phone", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();


                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                        break;
                    }
                    return sb.toString();
                }catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        checkIdData task = new checkIdData();
        task.execute(Id);

        return true;
    }

    public void insert(View v) {

        String Id = editTextId.getText().toString();
        String Pw = editTextPw.getText().toString();
        String Name = editTextName.getText().toString();

        if(Id != null && !Id.equals("") && Pw != null && !Pw.equals("") && Name != null && !Name.equals("")){
            if(flagId.equals("idokay")){
                insertToDatabase(Id, Pw, Name);
                startActivity(new Intent(JoinActivity.this, LoginActivity.class));
            }else{
                makeToast("중복체크버튼을 눌러주세요", getResources().getDrawable(R.drawable.baseline_clear_white_24));
            }
        }else makeToast("모든 항목을 입력해주세요", getResources().getDrawable(R.drawable.baseline_clear_white_24));
    }



    private void insertToDatabase(String Id, String Pw, String Name) {
        class InsertData extends AsyncTask<String, Void, String> {
        @Override
            protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
            protected void  onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equals("success"))
                makeToast("회원가입 되었습니다", getResources().getDrawable(R.drawable.baseline_check_white_24));
            else
                makeToast("회원가입 취소", getResources().getDrawable(R.drawable.baseline_clear_white_24));
        }

            @Override
            protected String doInBackground(String... params) {

                try {
                    String Id = (String) params[0];
                    String Pw = (String) params[1];
                    String Name = (String) params[2];

                    String link = "http://13.124.94.107/join.php";
                    String data = URLEncoder.encode("Id", "UTF-8") + "=" + URLEncoder.encode(Id, "UTF-8");
                    data += "&" + URLEncoder.encode("Pw", "UTF-8") + "=" + URLEncoder.encode(Pw, "UTF-8");
                    data += "&" + URLEncoder.encode("Name", "UTF-8") + "=" + URLEncoder.encode(Name, "UTF-8");

                    URL url = new URL(link);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                }
            }
        }
        InsertData task = new InsertData();
        task.execute(Id, Pw, Name);
    }
}
