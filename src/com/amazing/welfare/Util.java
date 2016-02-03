package com.amazing.welfare;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

public class Util {
	
	
	/**
	 * print log
	 * @param log
	 */
	public static void println(String log){
		if(TextUtils.isEmpty(log)) {
			return ;
		}
		Log.i("welfare", log);
	}

	/**
	 * 获取versionCode
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context){
		try {
			PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 10000;
	}
	
	
	/**
	 * 获取
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context){
		try {
			PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "1.0";
	}

	/**
	 * 判断屏幕是否点亮
	 * @param context
	 */
	public static boolean isScreenOn(Context context){
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
		Util.println("isScreenOn " +isScreenOn);
		return isScreenOn;
	}
	
	/**
	 * 判断屏幕是否上锁
	 * @param context
	 * @return
	 */
	public static boolean isScreenLock(Context context){
		KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
		Util.println("isScreenLock " +flag);
		return flag;
	}
}
