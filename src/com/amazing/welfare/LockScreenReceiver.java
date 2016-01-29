package com.amazing.welfare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class LockScreenReceiver extends BroadcastReceiver{

    private static LockScreenReceiver lockScreenReceiver = new LockScreenReceiver();
    private LockScreenReceiver(){
        
    }
    
    public static LockScreenReceiver getIns(){
        return lockScreenReceiver;
    }
    public void regist(Context context) {
        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        filter.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(lockScreenReceiver, filter);
    }
    
    public void unRegist(Context context){
        context.unregisterReceiver(lockScreenReceiver);
    }
    
    private IScreenListener mIScreenListener = null;
    public void setScreenListener(IScreenListener listener) {
        mIScreenListener = listener;
    }
    
    @Override
    public void onReceive(Context arg0, Intent intent) {
      
      String  action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {           
            // 开屏
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { 
            if(mIScreenListener != null) {
                mIScreenListener.onScreenOff();
            }
            // 锁屏
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) { 
            // 解锁
        }
    }

}
