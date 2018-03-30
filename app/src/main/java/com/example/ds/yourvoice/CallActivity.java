package com.example.ds.yourvoice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.vidyo.VidyoClient.Connector.ConnectorPkg;
import com.vidyo.VidyoClient.Connector.Connector;
import com.vidyo.VidyoClient.Device.Device;
import com.vidyo.VidyoClient.Device.LocalCamera;
import com.vidyo.VidyoClient.Device.LocalMonitor;
import com.vidyo.VidyoClient.Device.LocalWindowShare;
import com.vidyo.VidyoClient.Endpoint.ChatMessage;
import com.vidyo.VidyoClient.Endpoint.Participant;

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
import java.util.logging.Logger;

public class CallActivity extends AppCompatActivity
        implements Connector.IConnect, Connector.IRegisterParticipantEventListener, Connector.IRegisterMessageEventListener {

    private Intent intent;

    private Connector vc;
    private String token;
    private String displayName, resourceId;
    private String connectUser;
    private String user;
    private FrameLayout videoFrame;
    private boolean mVidyoClientInitialized = false;

    enum VidyoConnectorState {
        VidyoConnectorStateConnected,
        VidyoConnectorStateDisconnected,
        VidyoConnectorStateDisconnectedUnexpected,
        VidyoConnectorStateFailure
    }

    private VidyoConnectorState mVidyoConnectorState = VidyoConnectorState.VidyoConnectorStateDisconnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call);

        intent = getIntent();

        ConnectorPkg.setApplicationUIContext(this);
        mVidyoClientInitialized = ConnectorPkg.initialize();
        videoFrame = (FrameLayout)findViewById(R.id.videoFrame);
    }

//    public void Start(View v) {
//        vc = new Connector(videoFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default, 15, "warning info@VidyoClient info@VidyoConnector", "", 0);
//        vc.showViewAt(videoFrame, 0, 0, videoFrame.getWidth(), videoFrame.getHeight());
//
//        vc.selectDefaultCamera();
//        vc.selectDefaultMicrophone();
//        vc.selectDefaultSpeaker();
//        vc.selectDefaultNetworkInterfaceForSignaling();
//        vc.selectDefaultNetworkInterfaceForMedia();
//}

    public void Connect(View v) {
            token = "cHJvdmlzaW9uAGluc2VvbkA0OTNmN2UudmlkeW8uaW8AMTYzNjg5NjA5MDM5AAAwMjA1NGY0YTIxOTRkYzQ1NTc1ZGM5NGVmZThjMTI3MGI1Yjk2Y2FmN2ZmYzAxYjJiOTZiYTY1ZGJiYjYwYmI2MTBmNGQ1MTcyNWI1NTQxMzY2NWNkZTFhNGViYzY1NWU=";

            user = intent.getStringExtra("userId");
            connectUser = v.getTag().toString();
            displayName = user + "-" + connectUser;
            Log.d("user->connectUser", connectUser + "->" + user);

//            try {
//
//            }

            if(mVidyoClientInitialized) {

//            if(startCall()) {
                vc = new Connector(videoFrame, Connector.ConnectorViewStyle.VIDYO_CONNECTORVIEWSTYLE_Default, 2, "warning info@VidyoClient info@VidyoConnector", "", 0);

                vc.showViewAt(videoFrame, 0, 0, videoFrame.getWidth(), videoFrame.getHeight());

                vc.selectDefaultCamera();
                vc.selectDefaultMicrophone();
                vc.selectDefaultSpeaker();
                vc.selectDefaultNetworkInterfaceForSignaling();
                vc.selectDefaultNetworkInterfaceForMedia();

                //mVidyoConnector.Connect(host, token, displayName, resourceId, this);
                vc.connect("prod.vidyo.io", token, "call", displayName, this);
//            }
        } else {
            Log.d("Initialize failed", "not constructing VidyoConnector");
        }
    }


    public void Disconnect(View v) {
        vc.disconnect();
    }

    public void onSuccess() {
        Log.d("onSuccess", "successfully connected.");
        //connectorStateUpdated(VidyoConnectorState.VidyoConnectorStateConnected, "Connected");



    }

    public void onFailure(Connector.ConnectorFailReason reason) {
        Log.d("onFailure", ": connection attempt failed, reason = " + reason.toString());

        // Update UI to reflect connection failed
        //connectorStateUpdated(VidyoConnectorState.VidyoConnectorStateFailure, "Connection failed");
    }

    public void onDisconnected(Connector.ConnectorDisconnectReason reason) {
        if (reason == Connector.ConnectorDisconnectReason.VIDYO_CONNECTORDISCONNECTREASON_Disconnected) {
            // Release device resources
            vc.disable();
            vc = null;

            // Uninitialize the VidyoClient library
            ConnectorPkg.uninitialize();

            Log.d("onDisconnected", "successfully disconnected, reason = " + reason.toString());
            //connectorStateUpdated(VidyoConnectorState.VidyoConnectorStateDisconnected, "Disconnected");
        } else {
            Log.d("onDisconnected", "unexpected disconnection, reason = " + reason.toString());
            //connectorStateUpdated(VidyoConnectorState.VidyoConnectorStateDisconnectedUnexpected, "Unexpected disconnection");
        }
    }

    // Register for VidyoConnector event listeners. Note: this is an arbitrary function name.
    public void RegisterForVidyoEvents() {
        // Register for Participant callbacks
        vc.registerParticipantEventListener(this);

        // Register to receive chat messages
        vc.registerMessageEventListener(this);

       /* Register for local window share and local monitor events */
//        vc.registerLocalWindowShareEventListener(this);
//        vc.registerLocalMonitorEventListener(this);
    }
    // Participant Joined
    public void onParticipantJoined(Participant participant) {
        Log.d("ParticipainJoined", "ture");
    }
    // Participant Left
    public void onParticipantLeft(Participant participant) {}
    // Ordered array of participants according to rank
    public void onDynamicParticipantChanged(ArrayList participants, ArrayList cameras) {}
    // Current loudest speaker
    public void onLoudestParticipantChanged(Participant participant, boolean audioOnly) {}

    // Register for VidyoConnector event listeners. Note: this is an arbitrary function name.
//    public void RegisterForVidyoEvents() {
//        // Register to receive chat messages
//        vc.registerMessageEventListener(this);
//    }

    private void SendChatMessage(String message) {
        vc.sendChatMessage(message);
    }
    // Message received from other participants
    public void onChatMessageReceived(Participant participant, ChatMessage chatMessage) {}

//    /* Register for VidyoConnector event listeners. Note: this is an arbitrary function name. */
//    public void RegisterForVidyoEvents() {
//        /* Register for local window share and local monitor events */
//        vc.registerLocalWindowShareEventListener(this);
//        vc.registerLocalMonitorEventListener(this);
//    }

    @Override
    protected void onDestroy() {
//        // Release device resources
//        vc.disable();
//        vc = null;
//
//        // Uninitialize the VidyoClient library
//        ConnectorPkg.uninitialize();

        Log.d("Destroy", "true");
        super.onDestroy();
    }

//    private boolean startCall(){
//        displayName, resourceId
//        class InsertData extends AsyncTask<String, Void, String> {
//            ProgressDialog loading;
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                loading = ProgressDialog.show(CallActivity.this, "login...", null, true, true);
//                //Toast.makeText(getApplicationContext(), "외않되", Toast.LENGTH_SHORT). show();
//            }
//
//            @Override
//            protected void  onPostExecute(String s) {
//                super.onPostExecute(s);
//                loading.dismiss();
//
//                String usermsg = "";
//                String userphone = "";
//
//                try {
//                    // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
//                    JSONObject jObject = new JSONObject(s);
//                    // results라는 key는 JSON배열로 되어있다.
//                    JSONArray results = jObject.getJSONArray("result");
//
//
//                    for ( int i = 0; i < results.length(); ++i ) {
//                        JSONObject temp = results.getJSONObject(i);
//                        usermsg =temp.get("usermsg").toString();
//                        userphone = temp.get("userphone").toString();
//
//                    }
//                    //Log.e("ssssssssssssssss",flagId.toString());
//                    Toast.makeText(getApplicationContext(), usermsg, Toast.LENGTH_SHORT). show();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                if(usermsg.equals("User Found")) {
//                    Intent intent = new Intent(CallActivity.this, MainActivity.class);
//                    intent.putExtra("userId", Id);
//                    intent.putExtra("userPhone", userphone);
//                    startActivity(intent);
//                }
//
//                else
//                    Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 확인해주세요", Toast.LENGTH_SHORT). show();
//            }
//
//            @Override
//            protected String doInBackground(String... params) {
//
//                try {
//                    String Id = (String) params[0];
//                    String Pw = (String) params[1];
//
//                    String link = "http://13.124.94.107/login.php";
//                    String data = URLEncoder.encode("Id", "UTF-8") + "=" + URLEncoder.encode(Id, "UTF-8");
//                    data += "&" + URLEncoder.encode("Pw", "UTF-8") + "=" + URLEncoder.encode(Pw, "UTF-8");
//
//                    URL url = new URL(link);
//                    URLConnection conn = url.openConnection();
//
//                    conn.setDoOutput(true);
//                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
//
//                    wr.write(data);
//                    wr.flush();
//
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//
//                    StringBuilder sb = new StringBuilder();
//                    String line = null;
//
//                    // Read Server Response
//                    while ((line = reader.readLine()) != null) {
//                        sb.append(line + "\n");
//                        break;
//                    }
//                    return sb.toString();
//                } catch (Exception e) {
//                    return new String("Exception: " + e.getMessage());
//                }
//            }
//        }
//        InsertData task = new InsertData();
//        task.execute(Id, Pw);
//    }
}
