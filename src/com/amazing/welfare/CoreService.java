
package com.amazing.welfare;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class CoreService extends AccessibilityService implements IScreenListener{

	private static final String WELFARE_KEY = "[微信红包]";
	private int counts = 0;
	private Handler handler =new Handler(Looper.getMainLooper());
	public static boolean isAutoBackWeechat = false;
	public static String autoString = "";
	public static String autoString2 = "";
	public static String autoString3 = "";
	public static String autoString4 = "";
	public static String autoString5 = "";
	public static boolean isOpenMyself = true;
	public static boolean isAutoReplay = true;
	private boolean isSuspend = false;
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if(isSuspend) {
			return;
		}

		int type = event.getEventType();
		switch (type) {
			case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED://通知栏状态有变化
				Util.println(" AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED");
				List<CharSequence> list = event.getText();
				for (CharSequence charSequence : list) { //遍历温馨通知栏并打开通知
					if(charSequence.toString().contains(WELFARE_KEY)){
						openNotification(event);
						break;
					}
				}
				break;
			case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: //窗口状态有变
				weechatWindowChange(event);
				break;
	
			default:
				break;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		LockScreenReceiver.getIns().regist(getApplicationContext());//监听屏幕锁屏事件
		LockScreenReceiver.getIns().setScreenListener(this);
		updateNotification();
		Util.println("onCreate ");
		Toast.makeText(this, "已启动抢红包功能", Toast.LENGTH_SHORT).show();
	}

	
	private void updateNotification(){
		Notification notification = new Notification(R.drawable.ic_launcher, "红包助手开始运行",
		        System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, CoreService.class);
		notificationIntent.setFlags(5);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
		notificationIntent.putExtra("key", "key");
		notification.setLatestEventInfo(this, "红包助手", isSuspend?"已暂停，点击恢复抢红包": "已抢" + counts + "个红包,点击暂停", pendingIntent);
		startForeground(101, notification);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		isSuspend = !isSuspend;
		updateNotification();
		return super.onStartCommand(intent,flags,startId) ;
	}
	
	/**
	 * 打开微信红包
	 */
	private void openNotification(AccessibilityEvent event){
		Util.println("openNotification");
		if(event== null) {
			return;
		}
		if(event.getParcelableData() == null) {
			return;
		}
		//每次打开时清空缓存
		clear();
	 	try {
			 if(Util.isScreenLock(getApplicationContext()) || !Util.isScreenOn(getApplicationContext())) {
				 Intent intent = new Intent(this, UnlockScreenService.class);
				 Util.println("will be unlock screen");
				 startService(intent);
			 }
		    Notification notification = (Notification) event.getParcelableData();
		    PendingIntent pendingIntent = notification.contentIntent;
            pendingIntent.send();
            /*
             * 若是正在聊天界面时，另外一人发红包给你时，这时会自动点击通知，但是由于
             * 没触发AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED 所以不会去寻找红包，这时我将设置个200毫秒延时，若是200毫秒后都还没有触发就去查找红包
             */
            handler.postDelayed(findWalfareRunnable, 200);
        } catch (Exception e) {
            e.printStackTrace();
            Util.println("openNoticication " + e.toString());
        }
	}

	private Runnable backMain = new Runnable() {
		@Override
		public void run() {
			backHome();
			Toast.makeText(CoreService.this,"将微信退到后台，可以在红包助手设置",Toast.LENGTH_SHORT).show();
		}
	};
	/**
	 * 打开红包
	 * @param event
	 */
	private void weechatWindowChange(AccessibilityEvent event) {
		if(event == null) {
			return;
		}
		 CharSequence curClass = event.getClassName();
     	 Util.println("weechatWindowChange " + curClass);
		 if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(curClass)) { //弹出红包界面
			openWelfare();
		 } else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(curClass)) { //红包详情页面
			 backCurrentWindow();
		 } else if("com.tencent.mm.ui.LauncherUI".equals(curClass)) { //聊天界面
			handler.removeCallbacks(findWalfareRunnable);
			findWalfareRunnable.run();
			if(isAutoBackWeechat) {
				handler.removeCallbacks(backMain);
				handler.postDelayed(backMain, 3000);
			}
			
			if(isGetWelf && isAutoReplay ) { //如果拆了红包且回到聊天界面时自动回复。
				sayThanks();
			}
			
		 }else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyPrepareUI".equals(curClass)) { //若是点击发红包页面则清空
			 clear();
		 }
	}
	
	private void clear(){
		mClickedWelfareList.clear();
     	Util.println("mClickedWelfareList.clear() ");

	}
	
	private Runnable findWalfareRunnable = new Runnable() {
		
		@Override
		public void run() {
			 findWelFare("领取红包");
			 if(isOpenMyself) {
				 findWelFare("查看红包");
			 }
		}
	};
	/**
	 * 返回
	 */
	private void backCurrentWindow(){
	      performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);  
	      Util.println("backCurrentWindow GLOBAL_ACTION_BACK ");
	}
	
	
	private void backHome(){
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);  
        Util.println("backCurrentWindow GLOBAL_ACTION_HOME ");

	}
	
	
	private List<AccessibilityNodeInfo> mClickedWelfareList = new ArrayList<AccessibilityNodeInfo>();
	private void findWelFare(String key) {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {  
        	Util.println("findWelFare getRootInActiveWindow null");
            return;  
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(key);
        for (AccessibilityNodeInfo nodepar : list) {
        	if(nodepar != null) {
           	 nodepar = nodepar.getParent();
           	 if(nodepar != null) {
              	if(!mClickedWelfareList.contains(nodepar)) {
             		mClickedWelfareList.add(nodepar);
                 	Util.println("findWelFare ACTION_CLICK ");
                 	nodepar.performAction(AccessibilityNodeInfo.ACTION_CLICK);  
             	}
           	 }
        	}
		}
	}
	

	private boolean isGetWelf = false;
	private void openWelfare() {
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {  
        	Util.println("findWelFare openWelfare null");
            return;  
        }
        
        /**
         * 由于android api18(4.4)才支持findAccessibilityNodeInfosByViewId(),为了适配更多的机型采用如此方法
         */
        for (int i = 0 ;i < nodeInfo.getChildCount(); ++i) {
        	AccessibilityNodeInfo info = nodeInfo.getChild(i);
        	if("android.widget.Button".equals(info.getClassName())){
        		info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        		counts++;
				MobclickAgent.onEvent(this,"openWelfare");
        		updateNotification();
        		isGetWelf = true;
        		return;
        	}
		}
        //若是下手慢了则自动退出当前
        backCurrentWindow();
	}

	/**
	 * 获取随机索引
	 * @return
	 */
	private int getRandomIndex(){
		Random random = new Random();
		return  random.nextInt(5) +1;
	}

	public String getRandomSay(){
		String say = "";
		int random = getRandomIndex();
		switch (random) {
			case 1:
				say = autoString;
				break;
			case 2:
				say = autoString2;
				break;
			case 3:
				say = autoString3;
				break;
			case 4:
				say = autoString4;
				break;
			case 5:
				say = autoString5;
				break;
		}
		return say;
	}


	private boolean isFindEdit = false;
	/**
	 * 自动回复
	 */
	private void sayThanks(){
		String say = getRandomSay();
		if(TextUtils.isEmpty(say)) {
			Toast.makeText(this, "红包助手自动回复为空", Toast.LENGTH_SHORT).show();
			return;
		}
		isGetWelf = false;
		final AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		isFindEdit = false;
		parste(nodeInfo,say);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				clickSend();
			}
		}, 200);
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) 
	private void parste(AccessibilityNodeInfo nodeInfo, String say){
		if (isFindEdit) {
			return ;
		}
		if(nodeInfo == null) return;
	      for (int i = 0 ;i < nodeInfo.getChildCount(); ++i) {
	        	AccessibilityNodeInfo info = nodeInfo.getChild(i);
	        	if(info != null) {
	            	if(info.getChildCount() > 0) {
	            		parste(info,say);
		        	}else {
		        	   	if("android.widget.EditText".equals(info.getClassName())){
		        	   		ClipboardManager clipboard =  (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		        	   		clipboard.setText(say);
		        	   		info.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
		        	   		info.performAction(AccessibilityNodeInfo.ACTION_PASTE);
			        		isFindEdit = true;
			        		return;
			        	}
					}
	        	}
			}
	}
	

	private void clickSend(){
		AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {  
        	Util.println("clickSend clickSend null");
            return;  
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("发送");
        for (AccessibilityNodeInfo nodepar : list) {
        	if(nodepar != null) {
           	 if(nodepar != null) {
	        		Util.println("发送clickSend送");
	        		nodepar.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                 	nodepar.performAction(AccessibilityNodeInfo.ACTION_CLICK);  
           	 }
        	}
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Toast.makeText(this, "已停止抢红包功能", Toast.LENGTH_SHORT).show();

	}
	@Override
	public void onInterrupt() {
		Toast.makeText(this, "已停止抢红包功能", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
	}

    @Override
    public void onScreenOff() {
//        openMainActivity();//打开activity，防止后台被杀
//        backHome();//当屏幕黑屏时返回到home界面这样下次才能收到微信通知
    }
    
    private void openMainActivity(){ 
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
