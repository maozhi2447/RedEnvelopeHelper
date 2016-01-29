
package com.amazing.welfare;

import java.util.ArrayList;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

public class CoreService extends AccessibilityService implements IScreenListener{

	private static final String WELFARE_KEY = "[微信红包]";
	private int counts = 0;
	private boolean isRunning = true;
	private Handler handler =new Handler(Looper.getMainLooper());
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int type = event.getEventType();
		switch (type) {
			case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED://通知栏状态有变化
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
		// TODO Auto-generated method stub
		super.onCreate();
		LockScreenReceiver.getIns().regist(getApplicationContext());//监听屏幕锁屏事件
		LockScreenReceiver.getIns().setScreenListener(this);
		startForeground();
		Util.println("onCreate " );
	}
	
	private void startForeground(){
		updateNotification();
	}
	
	private void updateNotification(){
		Notification notification = new Notification(R.drawable.ic_launcher, "红包助手开始运行",
		        System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, CoreService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, "红包助手", isRunning ? "已抢"+counts+"个,点击暂停抢红包服务" :"已暂停抢红包，点击启动", pendingIntent);
		Toast.makeText(this, isRunning ? "已启动抢红包功能":"已暂停抢红包功能", Toast.LENGTH_SHORT).show();
		startForeground(101, notification);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(isRunning ) {
			isRunning = false;
		}else {
			isRunning = true;
		}
		updateNotification();
		return super.onStartCommand(intent, flags, startId);
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

	private Runnable lockScreen = new Runnable() {
        
        @Override
        public void run() {
            Intent intent = new Intent(getApplicationContext(), UnlockScreenService.class);
            stopService(intent);
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
		if(!isRunning) {
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
			handler.removeCallbacks(lockScreen);
			handler.postDelayed(lockScreen, 2000);
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
			 findWelFare("查看红包");
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
        		return;
        	}
		}
        //若是下手慢了则自动退出当前
        backCurrentWindow();
	}

	@Override
	public void onInterrupt() {
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
	}

    @Override
    public void onScreenOff() {
        openMainActivity();//打开activity，防止后台被杀
//        backHome();
    }
    
    private void openMainActivity(){ 
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
