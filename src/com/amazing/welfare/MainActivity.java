package com.amazing.welfare;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;

import org.json.JSONObject;

import java.util.List;



public class MainActivity extends Activity implements UmengOnlineConfigureListener {

	private Button button = null;
    private Button updateButton = null;
    private final Intent mAccessibleIntent =
            new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    private CheckBox checkBox = null;
    private CheckBox myself = null;
    private CheckBox replay = null;
    private TextView versionTextView = null;
    private EditText editText = null;
    private EditText editText2 = null;
    private EditText editText3 = null;
    private EditText editText4 = null;
    private EditText editText5 = null;

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
                startActivity(mAccessibleIntent);
            }
        });
        
        
        checkBox = (CheckBox)findViewById(R.id.auto);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                CoreService.isAutoBackWeechat = arg1;
            }
        });
        
        myself = (CheckBox)findViewById(R.id.myself);
        myself.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				CoreService.isOpenMyself = arg1;
			}
		});
        

        editText = (EditText)findViewById(R.id.edit);
        editText.setSingleLine(true);
        editText.setText(getReplay(1));

        editText2 = (EditText)findViewById(R.id.edit2);
        editText2.setSingleLine(true);
        editText2.setText(getReplay(2));

        editText3 = (EditText)findViewById(R.id.edit3);
        editText3.setSingleLine(true);
        editText3.setText(getReplay(3));

        editText4 = (EditText)findViewById(R.id.edit4);
        editText4.setSingleLine(true);
        editText4.setText(getReplay(4));

        editText5 = (EditText)findViewById(R.id.edit5);
        editText5.setSingleLine(true);
        editText5.setText(getReplay(5));


        replay = (CheckBox)findViewById(R.id.replay);
        if(Build.VERSION.SDK_INT < 18) { //低于API18无法开启自动回复功能
            replay.setChecked(false);
            CoreService.isAutoReplay = false;
            replay.setEnabled(false);
            TextView textView = (TextView)findViewById(R.id.check);
            textView.setText("android4.3以上才支持自动回复功能");
            editText.setEnabled(false);
        }else {
            replay.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    CoreService.isAutoReplay = arg1;
                }
            });
        }
        
        updateButton = (Button)findViewById(R.id.updateBtn);
        updateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUpdate();
            }
        });
        
        versionTextView = (TextView)findViewById(R.id.version);
        versionTextView.setText("当前版本：" + Util.getVersionName(this));
    }

    private String getReplay(int id){
    	SharedPreferences sharedPreferences = getSharedPreferences("replay", Context.MODE_PRIVATE);
        String str = "";
        switch (id) {
            case 1:
                str = sharedPreferences.getString("key" + id, "多谢老板，棒棒哒！");
                break;
            case 2:
                str = sharedPreferences.getString("key" + id, "终于抢到了，不容易啊！");
                break;
            case 3:
                str = sharedPreferences.getString("key" + id, "蚊子腿也是肉呀。");
                break;
            case 4:
                str = sharedPreferences.getString("key" + id, "努力抢红包攒苹果6！");
                break;
            case 5:
                str = sharedPreferences.getString("key" + id, "谢谢");
                break;
        }
    	return str;
    }


    private void saveReplay(int id){
    	SharedPreferences sharedPreferences = getSharedPreferences("replay", Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = sharedPreferences.edit();
    		String str = "";
            switch (id) {
                case 1:
                    str =editText.getText().toString();
                    CoreService.autoString = str;
                    break;
                case 2:
                    str =editText2.getText().toString();
                    CoreService.autoString2 = str;
                    break;
                case 3:
                    str =editText3.getText().toString();
                    CoreService.autoString3 = str;
                    break;
                case 4:
                    str =editText4.getText().toString();
                    CoreService.autoString4 = str;
                    break;
                case 5:
                    str =editText5.getText().toString();
                    CoreService.autoString5 = str;
                    break;
            }
        	editor.putString("key" + id, str);

    	editor.commit();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        updateServiceStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        saveReplay(1);
        saveReplay(2);
        saveReplay(3);
        saveReplay(4);
        saveReplay(5);
    }

    private void updateServiceStatus() {
        boolean serviceEnabled = false;

        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getPackageName() + "/.CoreService")) {
                serviceEnabled = true;
            }
        }
        button.setText(serviceEnabled ? "已开启，点击去关闭" : "已停止，点击去开启");
        checkBox.setChecked(CoreService.isAutoBackWeechat);
        replay.setChecked(CoreService.isAutoReplay);
        myself.setChecked(CoreService.isOpenMyself);

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
