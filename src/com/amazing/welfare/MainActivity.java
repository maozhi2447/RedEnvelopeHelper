package com.amazing.welfare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;

import org.json.JSONObject;



public class MainActivity extends Activity implements UmengOnlineConfigureListener {

	private Button button = null;
    private Button updateButton = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobclickAgent.setOnlineConfigureListener(this);
        MobclickAgent.updateOnlineConfig(this);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });

        updateButton = (Button)findViewById(R.id.updateBtn);
        updateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUpdate();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAbout(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = MobclickAgent.getConfigParams(this, "about");
        if(TextUtils.isEmpty(message)) {
            message = "一个无聊的人 @毛胖子";
        }
        builder.setTitle("关于").setMessage(message);
        builder.create().show();
    }
    
    private boolean isNeedUpdate() throws  Exception{
        int iCurCode= Util.getVersionCode(this);
        String code = MobclickAgent.getConfigParams(this, "versionCode");
        if(TextUtils.isEmpty(code)) {
            return false;
        }
        int iCode = Integer.parseInt(code);
        if(iCurCode < iCode) {
            return true;
        }
        return false;
    }

    private void checkUpdate(){
        try{
            boolean isNeedUpdate = isNeedUpdate();
            if(isNeedUpdate) {
                showUpdateDialog();
            }else{
                showNoNeedUpdate();
            }
        }catch (Exception e) {
            e.printStackTrace();
            Util.println("checkUpdate  " + e.toString());
        }
    }
    
    private void showNoNeedUpdate(){
       Toast.makeText(this, "当前已是最新版本", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = MobclickAgent.getConfigParams(this, "titie");
        String message = MobclickAgent.getConfigParams(this, "message");
        String confirm = MobclickAgent.getConfigParams(this, "confirm");
        String cancel = MobclickAgent.getConfigParams(this, "cancel");
       final String url = MobclickAgent.getConfigParams(this, "url");

        builder.setTitle(title).setMessage(message).setCancelable(false);
        builder.setPositiveButton(confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private Handler mHandle = new Handler(Looper.getMainLooper());
    @Override
    public void onDataReceived(JSONObject jsonObject) {
        mHandle.post(new Runnable() {
            @Override
            public void run() {
                checkUpdate();
            }
        });
    }
}
