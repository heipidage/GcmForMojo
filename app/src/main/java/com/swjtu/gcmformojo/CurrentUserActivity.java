package com.swjtu.gcmformojo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.huawei.hms.support.api.push.TokenResult;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.swjtu.gcmformojo.MyApplication.SYS;
import static com.swjtu.gcmformojo.MyApplication.deviceGcmToken;
import static com.swjtu.gcmformojo.MyApplication.deviceMiToken;
import static com.swjtu.gcmformojo.MyApplication.getCurTime;
import static com.swjtu.gcmformojo.MyApplication.miSettings;
import static com.swjtu.gcmformojo.MyApplication.mi_APP_ID;
import static com.swjtu.gcmformojo.MyApplication.mi_APP_KEY;
import static com.swjtu.gcmformojo.MyApplication.mySettings;

public class CurrentUserActivity extends Activity {

    private ArrayList<User> currentUserList;
    public static Handler userHandler;
    public RecyclerView currentUserListView;
    public UserAdapter currentUserAdapter;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    final private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getOverflowMenu();

        currentUserListView = findViewById(R.id.current_user_list_view);
        currentUserListView.setLayoutManager(new LinearLayoutManager(this));

        verifyStoragePermissions(this);

        userHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                String handlerMsg = (String)msg.obj;

                if(handlerMsg.equals("UpdateCurrentUserList") && currentUserAdapter!=null){
                    currentUserAdapter.notifyDataSetChanged();
                }

                super.handleMessage(msg);
            }
        };

        //SharedPreferences Settings = getSharedPreferences(PREF, Context.MODE_PRIVATE);
        final String qqReplyUrl=mySettings.getString("edit_text_preference_qq_replyurl","");
        final String wxReplyUrl=mySettings.getString("edit_text_preference_wx_replyurl","");
        final  String qqPackgeName=mySettings.getString("edit_text_preference_qq_packgename","com.tencent.mobileqq");
        final  String wxPackgeName=mySettings.getString("edit_text_preference_wx_packgename","com.tencent.mm");

        String pushType=mySettings.getString("push_type","GCM");
        switch (pushType){
            case "GCM":
                deviceGcmToken = FirebaseInstanceId.getInstance().getToken();
                //stopMiPush();
               // Log.e(MYTAG, "使用GCM推送");
                break;
            case "MiPush":
                if(shouldInit()) {
                    MiPushClient.registerPush(this, mi_APP_ID, mi_APP_KEY);
                }
                //SharedPreferences miSettings =        getSharedPreferences("mipush", Context.MODE_PRIVATE);
                deviceMiToken = miSettings.getString("regId",deviceMiToken);
               // Log.e(MYTAG, "使用MiPush推送");
                break;
            case "HwPush":
                //调用connect方法前需要init，已在MyApplication中作相应处理
                HMSAgent.connect(this, new ConnectHandler() {
                    @Override
                    public void onConnect(int rst) {
                    }
                });
                //getToken只有在服务端开启push服务后才会返回成功
                //deviceHwToken将会以广播方式返回，已在HuaweiPushRevicer中作相应处理
                HMSAgent.Push.getToken(new GetTokenHandler() {
                    public void onResult(int rtnCode, TokenResult tokenResult) {
                        //Log.e("get token: end" + rtnCode);
                    }
                });
                //stopMiPush();
                //  Log.e(MYTAG, "使用HwPush推送");
                break;
            default:
                deviceGcmToken = FirebaseInstanceId.getInstance().getToken();
                //stopMiPush();
              //  Log.e(MYTAG, "默认DefaultGCM推送");
                break;

        }

        addNotfiyContent();

        currentUserAdapter = new UserAdapter(currentUserList);
        currentUserListView.setAdapter(currentUserAdapter);
        //currentUserListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


        currentUserAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                User p=currentUserList.get(position);
                Intent intentSend = new Intent(getApplicationContext(), DialogActivity.class);
                intentSend.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Bundle msgDialogBundle = new Bundle();
                msgDialogBundle.putString("msgId",p.getUserId());
                msgDialogBundle.putString("qqReplyUrl",qqReplyUrl);
                msgDialogBundle.putString("wxReplyUrl",wxReplyUrl);
                msgDialogBundle.putString("senderType", p.getSenderType());
                msgDialogBundle.putString("msgType",p.getUserType());
                msgDialogBundle.putString("msgTitle",p.getUserName());
                msgDialogBundle.putString("msgBody",p.getUserMessage());
                msgDialogBundle.putInt("notifyId", p.getNotifyId());
                msgDialogBundle.putString("msgTime",p.getUserTime());
                msgDialogBundle.putString("qqPackgeName",qqPackgeName);
                msgDialogBundle.putString("wxPackgeName",wxPackgeName);
                intentSend.putExtras(msgDialogBundle);

                startActivity(intentSend);
            }

            @Override
            public boolean onItemLongClick(AdapterView<?> parent,View view,int position,long id) {

                //系统信息不能删除
                if(!currentUserList.get(position).getUserId().equals("0") && !currentUserList.get(position).getUserId().equals("1") && !currentUserList.get(position).getUserId().equals("2")) {
                    currentUserList.remove(position);
                    currentUserAdapter.notifyDataSetChanged();
                }
                return true;//当返回true时,不会触发短按事件
                //return false;//当返回false时,会触发短按事件
            }
        });
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    public void addNotfiyContent() {

        //点击通知增加会话内容，用于缓存被杀时列表内容为空的情况，使用通知自带的最后一条消息

        Intent intentCurrentListUser = getIntent();
        if(intentCurrentListUser!=null) {
            Bundle msgBundle = intentCurrentListUser.getExtras();
            if (msgBundle != null && msgBundle.containsKey("userId")) {

                if (msgBundle.getInt("notifyId") != -1) {
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(msgBundle.getInt("notifyId"));
                 /*   for(int    i=0;    i<currentUserList.size();    i++){
                        if(currentUserList.get(i).getNotifyId()==notifyId){
                            currentUserList.get(i).setMsgCount("0");
                            if(CurrentUserActivity.userHandler!=null)
                                new WeixinNotificationBroadcastReceiver.userThread().start();
                            break;
                        }
                    }
                    */

                }

                if (!isHaveMsg(currentUserList, msgBundle.getString("userId"))) {
                    User noifyMsg = new User(msgBundle.getString("userName"), msgBundle.getString("userId"), msgBundle.getString("userType"), msgBundle.getString("userMessage"), msgBundle.getString("userTime"), msgBundle.getString("senderType"), msgBundle.getInt("NotificationId"), msgBundle.getString("msgCount"));
                    currentUserList.add(0, noifyMsg);

                    if (currentUserAdapter != null) {
                        currentUserAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.action_qq_contacts:
                Intent intentFriend = new Intent(this,QqContactsActivity.class);
                startActivity(intentFriend);
                break;
            case R.id.action_wechat_contacts:
                Intent intentWechatFriend = new Intent(this,WechatContactsActivity.class);
                startActivity(intentWechatFriend);
                break;
            case R.id.action_settings:
                Intent intentSettings = new Intent(this, FragmentPreferences.class);
                startActivity(intentSettings);
                break;
            case R.id.action_help:
                Intent intentHelp = new Intent(this, HelpActivity.class);
                startActivity(intentHelp);
                break;
            case R.id.action_token:
                DialogFragment newFragment = new TokenDialog();
                newFragment.show(getSupportFragmentManager(), "token");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

// --Commented out by Inspection START (2017/2/27 19:24):
//    public Handler getHandler(){
//        return userHandler;
//    }
// --Commented out by Inspection STOP (2017/2/27 19:24)

    @Override
    protected void onResume() {

        super.onResume();

        //进入界面清除通知
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        //由于机器人接受消息后会变成SYS状态，此处直接进行更改，并在MyApplication内做相应处理
        if (!isHaveMsg(currentUserList,"2"))
            currentUserList.add(new User(getString(R.string.user_bot_wechat_name), "2", SYS, getString(R.string.user_bot_msg), getCurTime(), "1", 2, "0"));
        if (!isHaveMsg(currentUserList,"1"))
            currentUserList.add(new User(getString(R.string.user_bot_qq_name), "1", SYS, getString(R.string.user_bot_msg), getCurTime(), "1", 1, "0"));
        if (!isHaveMsg(currentUserList,"0"))
            currentUserList.add(new User(getString(R.string.user_welcome_name), "0", SYS, getString(R.string.user_welcome_msg), getCurTime(), "1", 0, "0"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //getApplicationContext().unregisterReceiver();

      // unregisterReceiver("com.xiaomi.push.service.receivers.NetworkStatusReceiver");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        addNotfiyContent();
    }

    public  Boolean  isHaveMsg(final ArrayList<User> userList,final String userId){
        if(userList.size()==0) {
            return false;
        }
        for(int    i=0;    i<userList.size();    i++){
            String str = userList.get(i).getUserId();

            if(str.equals(userId)){
                return true;
            }
        }
        return false;
    }

    /**
     * force to show overflow menu in action bar (phone) by
     * http://blog.csdn.net/jdsjlzx/article/details/36433441
     */
    public void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
