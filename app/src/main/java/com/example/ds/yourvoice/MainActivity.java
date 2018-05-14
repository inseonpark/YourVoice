package com.example.ds.yourvoice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
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
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements ListViewAdapter.ListBtnClickListener {

    Toolbar myToolbar;
    EditText addId;
    Intent intent;
    public static String userId;

    /* 리스트뷰 변수 */
    ArrayList<HashMap<String, String>> mArrayList;
    String mJsonString;
    ListView listview;  //MainActivity에서 리스트뷰 위치
    ListViewAdapter adapter;
    ArrayList<ListViewItem> list = new ArrayList<ListViewItem>(); //실질적인 listview

    public static Context context;
    public Intent cIntent;

    private static String TAG = "FRIENDLIST";

    private static final String TAG_JSON = "friendList";
    private static final String TAG_NAME = "friendname";
    private static final String TAG_ID = "friendid";

    public static final String CALL_RECEIVER = "receiver";
    private IntentFilter mIntentFilter;
    private RestartService restartService;

    private static PowerManager.WakeLock sCpuWakeLock;

    enum CallStatus {
        Default,
        Caller,
        Receiver
    }

    public CallStatus callStatus = CallStatus.Default;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //서비스시작
        restartService = new RestartService();
        IntentFilter intentFilter = new IntentFilter(".CallService");
        registerReceiver(restartService, intentFilter);

        cIntent = new Intent(this, CallService.class);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(CALL_RECEIVER);
        registerReceiver(mReceiver, mIntentFilter);

        startService(cIntent);

        context = this;

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");


        // 추가된 소스, Toolbar를 생성한다.
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        //TabHost tabHost1 = getTabHost();

        TabHost.TabSpec ts1 = tabHost.newTabSpec("Tab1");
        ts1.setContent(R.id.tab_friendList);
        ts1.setIndicator("친구목록");
        tabHost.addTab(ts1);

        TabHost.TabSpec ts2 = tabHost.newTabSpec("Tab2");
        ts2.setContent(R.id.tab_recentCall);
        ts2.setIndicator("최근통화기록");
        tabHost.addTab(ts2);

        TabHost.TabSpec ts3 = tabHost.newTabSpec("Tab3");
        //ts3.setContent(new Intent(MainActivity.this, AddFriendActivity.class));
        ts3.setContent(R.id.tab_addFriend);
        // ts3.setContent(new Intent(MainActivity.this, AddFriendActivity.class));
        ts3.setIndicator("친구추가");
        tabHost.addTab(ts3);

        //로그인 했을때 처음 화면 설정
        tabHost.setCurrentTab(0);
        mArrayList = new ArrayList<>();
        friendData task = new friendData();
        task.execute("http://13.124.94.107/getFriendList.php");


        //탭 변경
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                // TODO Auto-generated method stub
                String strMsg;
                strMsg = tabId;
                //Toast.makeText(getApplicationContext(), strMsg, Toast.LENGTH_SHORT). show();

                if (strMsg.equals("Tab1")) {
                    /* 기존에 있던 어댑터 삭제*/
                    adapter.clear();
                    /* 아이템 추가 및 어댑터 등록 */
                    mArrayList = new ArrayList<>();

                    friendData task = new friendData();
                    task.execute("http://13.124.94.107/getFriendList.php");
                }
            }
        });

    }

    /* ---------------------------------------------- 서비스와 브로드캐스트리시버 ---------------------------------------------------------- */

    @Override
    public void onResume() {
        super.onResume();
        //registerReceiver(mReceiver, mIntentFilter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent mintent) {
            //전화받기화면
            if (mintent.getAction().equals(CALL_RECEIVER) && callStatus==CallStatus.Default) {

                stopService(cIntent);

                //액티비티 깨우기
                /*if (sCpuWakeLock != null) {
                    return;
                }
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                sCpuWakeLock = pm.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.ON_AFTER_RELEASE, "hi");

                sCpuWakeLock.acquire();


                if (sCpuWakeLock != null) {
                    sCpuWakeLock.release();
                    sCpuWakeLock = null;
                }*/


                //출처: http://cofs.tistory.com/173 [CofS]

                Log.d("전화받기화면", "전화받기화면");

                callStatus = CallStatus.Receiver;
                //stopService(cIntent);

                final RelativeLayout r = (RelativeLayout)findViewById(R.id.callReceiveLayout);
                r.setVisibility(View.VISIBLE);
                Button b = (Button)findViewById(R.id.callReceive);
                Button bt = (Button)findViewById(R.id.callDenial);
                //b.setEnabled(true);
                //bt.setEnabled(true);

                //전화받기버튼 누르기
                b.setOnClickListener(
                        new Button.OnClickListener() {
                            public void onClick(View v) {
                                r.setVisibility(View.INVISIBLE);
                                callingUpdate(mintent.getStringExtra("receiverID"), "1");
                                call(mintent.getStringExtra("callerID"), mintent.getStringExtra("receiverID"));
                                //stopService(cIntent);
                            }
                        }
                );

                //전화끊기버튼 누르기
                bt.setOnClickListener(
                        new Button.OnClickListener() {
                            public void onClick(View v) {
                                r.setVisibility(View.INVISIBLE);
                                callingUpdate(mintent.getStringExtra("callerID"), "0");
                                recreate();
                                //stopService(cIntent);
                            }
                        }
                );

                //call(intent.getStringExtra("callerID"), intent.getStringExtra("receiverID"));
                //Intent stopIntent = new Intent(MainActivity.this, CallService.class);
                //stopService(stopIntent);
            }
            //                Intent stopIntent = new Intent(MainActivity.this, BroadcastService.class);
            //                stopService(stopIntent);
        }
    };

    @Override
    protected void onPause() {
        //unregisterReceiver(mReceiver);
        super.onPause();
    }

     /* ---------------------------------------------- 서비스와 브로드캐스트리시버 끝 ---------------------------------------------------------- */


    /* ---------------------------------------------- 설정 버튼 ----------------------------------------------------------- */
    //추가된 소스, ToolBar에 menu.xml을 인플레이트함
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_settings1:
                // User chose the "Settings" item, show the app settings UI...
                Toast.makeText(getApplicationContext(), "환경설정 버튼 클릭됨", Toast.LENGTH_LONG).show();
                return true;
            case R.id.logout:
                new AlertDialog.Builder(this)
                        .setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                                //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(i);
                                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor editor = auto.edit();
                                editor.clear();
                                editor.commit();
                                Toast.makeText(MainActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                Toast.makeText(getApplicationContext(), "나머지 버튼 클릭됨", Toast.LENGTH_LONG).show();
                return super.onOptionsItemSelected(item);
        }
    }
    /* ---------------------------------------------- 설정 버튼 끝 ----------------------------------------------------------- */


    /* ---------------------------------------------- TAB1의 버튼 클릭 ----------------------------------------------------------- */
    @Override
    public void onListBtnClick(View v, final int position) {
        //전화버튼 눌렀을때
        if (v.getId() == R.id.button1) {
            ListViewItem friend = new ListViewItem();
            friend = (ListViewItem) adapter.getItem(position);
            String id = friend.getId();
            //Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
            call(id, v);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("친구 삭제")
                    .setMessage("삭제하시겠습니까?")
                    .setIcon(R.drawable.deletefriend)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 확인시 처리 로직
                            ListViewItem s = new ListViewItem();
                            s = (ListViewItem) adapter.getItem(position);
                            String id = s.getId();

                            deleteFriend(position, id);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 취소시 처리 로직
                        }
                    })
                    .show();
        }
    }

    /* ---------------------------------------------- TAB1의 버튼 클릭 끝----------------------------------------------------------- */


    /* ---------------------------------------------- DB에서 친구데이터 가져오기 ----------------------------------------------------------- */
    private class friendData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response  - " + result);

            if (result == null) {
            } else {
                mJsonString = result;
                showResult();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];

            try {
                //String link = "http://13.124.94.107/addFriend.php";
                String data = URLEncoder.encode("userId", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");

                URL url = new URL(serverURL);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
                return sb.toString().trim();

            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();
                return null;
            }

        }
    }

    /* ---------------------------------------------- DB에서 친구데이터 가져오기 끝 ----------------------------------------------------------- */


    /* ---------------------------------------------- 결과 LISTVIEW로 생성해서 보여주기 ----------------------------------------------------------- */
    private void showResult() {
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            //아이템 하나씩 만들어서 arraylist 형태로 저장
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject item = jsonArray.getJSONObject(i);

                String friendname = item.getString(TAG_NAME);
                String friendid = item.getString(TAG_ID);

                HashMap<String, String> hashMap = new HashMap<>();

                hashMap.put(TAG_NAME, friendname);
                hashMap.put(TAG_ID, friendid);

                mArrayList.add(hashMap);
            }


            ListViewItem item;

            //mArrayList에 있는 아이템들을 ListViewItem 형태로 저장해서 리스트뷰에 추가
            for (int i = 0; i < mArrayList.size(); i++) {
                HashMap<String, String> ritem = mArrayList.get(i);
                item = new ListViewItem();
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.icon));
                item.setName(ritem.get(TAG_NAME));
                item.setId(ritem.get(TAG_ID));
                list.add(item);
            }

            // Adapter 생성
            adapter = new ListViewAdapter(this, R.layout.listview_item, list, this);

            // 리스트뷰 참조 및 Adapter달기
            listview = (ListView) findViewById(R.id.friend_list_view);
            listview.setAdapter(adapter);

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }

    }

    /* ---------------------------------------------- 결과 LISTVIEW로 생성해서 보여주기 끝 ----------------------------------------------------------- */


    /* ---------------------------------------------- 친구 추가 ----------------------------------------------------------- */
    public void addFriend(View v) {
        addId = (EditText) findViewById(R.id.addFriendId);
        String friendId = addId.getText().toString();

        //LoginActivity loginActivity = (LoginActivity) getApplication();

        //String userId = intent.getStringExtra("userId");
        //Log.e("aaaaaaaaaaaaaaaaaaaa",userId);

        insertToDatabase(userId, friendId);

        //friendPhone.setText("");
        //friendPhone.requestFocus();
    }

    /* ---------------------------------------------- 친구 추가 끝----------------------------------------------------------- */


    /* ---------------------------------------------- 친구 삭제 ----------------------------------------------------------- */
    public void deleteFriend(int pos, String id) {
        //String userPhone = intent.getStringExtra("userId");
        deleteFromDatabase(pos, userId, id);
    }

    /* ---------------------------------------------- 친구 삭제 끝 ----------------------------------------------------------- */


    /* ---------------------------------------------- DB에 친구 번호 추가 ----------------------------------------------------------- */
    private void insertToDatabase(final String userId, String friendId) {
        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();

                addId.setText("");
                //addPhone.requestFocus();
            }

            @Override
            protected String doInBackground(String... params) {

                try {
                    String userId = (String) params[0];
                    String friendId = (String) params[1];


                    String link = "http://13.124.94.107/addFriend.php";
                    String data = URLEncoder.encode("UserId", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                    data += "&" + URLEncoder.encode("FriendId", "UTF-8") + "=" + URLEncoder.encode(friendId, "UTF-8");


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
        task.execute(userId, friendId);
    }

    /* ---------------------------------------------- DB에 친구 번호 추가 끝 ----------------------------------------------------------- */


    /* ---------------------------------------------- DB에 친구 번호 삭제 ----------------------------------------------------------- */
    private void deleteFromDatabase(final int pos, String userId, String friendId) {
        class DeleteData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                //리스트에서 삭제후 새로고침
                list.remove(pos);
                adapter.notifyDataSetChanged();
            }

            @Override
            protected String doInBackground(String... params) {

                try {
                    String userId = (String) params[0];
                    String friendId = (String) params[1];


                    String link = "http://13.124.94.107/deleteFriend.php";
                    String data = URLEncoder.encode("UserId", "UTF-8") + "=" + URLEncoder.encode(userId, "UTF-8");
                    data += "&" + URLEncoder.encode("FriendId", "UTF-8") + "=" + URLEncoder.encode(friendId, "UTF-8");


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
        DeleteData task = new DeleteData();
        task.execute(userId, friendId);
    }


    /* ---------------------------------------------- DB에 친구 번호 삭제 끝 ----------------------------------------------------------- */


    /* ---------------------------------------------- 전화 받기/끊기 ----------------------------------------------------------- */
    private void callingUpdate(final String Id, String num) {
        class InsertData extends AsyncTask<String, Void, String> {

            protected void onCancelled() {
                this.onCancelled();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if (s.toString().equals("success")) {
                    Log.d("callingUpdate success", "DB 업데이트 성공");
                } else
                    Log.d("callingUpdate error", "DB 업데이트 에러");

            }

            @Override
            protected String doInBackground(String... params) {

                if (this.isCancelled()) {
                    // 비동기작업을 cancel해도 자동으로 취소해주지 않으므로,
                    // 작업중에 이런식으로 취소 체크를 해야 한다.
                    return null;
                }

                try {
                    String Id = (String) params[0];
                    String num = (String) params[1];

                    String link = "http://13.124.94.107/callingUpdate.php";
                    String data = URLEncoder.encode("Id", "UTF-8") + "=" + URLEncoder.encode(Id, "UTF-8");
                    data += "&" + URLEncoder.encode("num", "UTF-8") + "=" + URLEncoder.encode(num, "UTF-8");

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
        task.execute(Id, num);
    }


    /* ---------------------------------------------- 전화 받기/끊기 끝 ----------------------------------------------------------- */


    /* ---------------------------------------------- 전화걸기 ----------------------------------------------------------- */
    public void call(String friendId, View v) {
        //String friendPhone = friendphone;
        Intent intent = new Intent(MainActivity.this, CallActivity.class);
        //intent.putExtra("Tag", v.getTag().toString());
        intent.putExtra("userId", userId);
        intent.putExtra("friendId", friendId);

        //startActivity(intent);
        startActivityForResult(intent, 0);
        stopService(cIntent);

        RelativeLayout r = (RelativeLayout)findViewById(R.id.callReceiveLayout);
        r.setVisibility(View.INVISIBLE);
    }

    public void call(String caller, String receiver) {
        Intent intent = new Intent(MainActivity.this, CallActivity.class);
        intent.putExtra("Caller", caller);
        intent.putExtra("Receiver", receiver);
        //startActivity(intent);
        startActivityForResult(intent, 0);
        stopService(cIntent);

        RelativeLayout r = (RelativeLayout)findViewById(R.id.callReceiveLayout);
        r.setVisibility(View.INVISIBLE);
    }

    /* ---------------------------------------------- 전화걸기 끝 ----------------------------------------------------------- */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {

            Log.d("CallActivitt Finish", "finish call");
            recreate();

            //startService(cIntent);

            //RelativeLayout r = (RelativeLayout)findViewById(R.id.callReceiveLayout);
            //r.setVisibility(View.INVISIBLE);

            //Intent cIntent = new Intent(this, CallService.class);
            //startService(cIntent);
        }
    }

    public String getUserId() {
        return userId;
    }
}