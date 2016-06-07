package com.mibound.datastatistic;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DataStatisticActivity extends Activity {

	private static final String TAG = "mijie";
	private static final String CONNECTIVITY_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";
	private static final String SHUT_DOWN = "android.intent.action.ACTION_SHUTDOWN";
	private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private static final int TYPE_WIFI = 1;
	private static final int TYPE_MOBILE = 2;
	private static final int TYPE_NULL = -1;
	
	private Button mRefrush;
	private Button mRead;
	private Button mWrite;
	private Button mClear;
	private Button mDataType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_statistic);
		mRefrush = (Button) findViewById(R.id.btn_refrush);
		mRefrush.setOnClickListener(listener);
		mRead = (Button) findViewById(R.id.btn_read);
		mRead.setOnClickListener(listener);
		mWrite = (Button) findViewById(R.id.btn_write);
		mWrite.setOnClickListener(listener);
		mClear = (Button) findViewById(R.id.btn_clear);
		mClear.setOnClickListener(listener);
		mDataType = (Button) findViewById(R.id.btn_data_type);
		mDataType.setOnClickListener(listener);
		//getTotal();
		registerBroadcast();
		// Log.i(TAG, "total: " + Formatter.formatFileSize(this, 1024));
	}

	public OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btn_refrush:
				List<AppItem> items = getTotalItems();
				for (int i = 0; i < items.size(); i++) {
					Log.i(TAG, "Name: " + items.get(i).getName() + "Usage: " + Formatter.formatFileSize(DataStatisticActivity.this,items.get(i).getUsage()));
				}
				break;
			case R.id.btn_read:
				AppItemDbHelper helper = new AppItemDbHelper(DataStatisticActivity.this);
				List<AppItem> itemsDB = helper.getAllAppItems();
				if (itemsDB.size() <= 0) {
					Log.i(TAG, "DB NULL");
				}else{
				for (int i = 0; i < itemsDB.size(); i++) {
					Log.i(TAG, "Name: " + itemsDB.get(i).getName() + "Usage: " + Formatter.formatFileSize(DataStatisticActivity.this,itemsDB.get(i).getUsage()));
				}}
				break;
			case R.id.btn_write:
				List<AppItem> itemsTotal = getTotalItems();
				updateToAppItemDB(itemsTotal);
				break;
			case R.id.btn_clear:
				AppItemDbHelper helperclear = new AppItemDbHelper(DataStatisticActivity.this);
				helperclear.deleteAllAppItems();
				break;
			case R.id.btn_data_type:
				int type = checkNetworkType();
				Log.i(TAG, "TYPE: " + type);
				break;

			default:
				break;
			}
		}
	};
	
	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		//filter.addAction(CONNECTIVITY_CHANGED);
		filter.addAction(BOOT_COMPLETED);
		filter.addAction(SHUT_DOWN);
		registerReceiver(receiver, filter);
	}

	private List<AppItem> getTotal() {
		List<AppItem> items = new ArrayList<AppItem>();
		// 1.获取一个包管理器。
		PackageManager pm = getPackageManager();
		// 2.遍历手机操作系统 获取所有的应用程序的uid
		List<ApplicationInfo> appliactaionInfos = pm
				.getInstalledApplications(0);
		AppItemDbHelper helper = new AppItemDbHelper(this);
		for (ApplicationInfo applicationInfo : appliactaionInfos) {
			int uid = applicationInfo.uid; // 获得软件uid
			String label = (String) pm.getApplicationLabel(applicationInfo);
			String packageName = applicationInfo.packageName;
			String[] permission = null;
			try {
				permission = pm.getPackageInfo(packageName,
						PackageManager.GET_PERMISSIONS).requestedPermissions;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// proc/uid_stat/10086
			long tx = TrafficStats.getUidTxBytes(uid);
			long rx = TrafficStats.getUidRxBytes(uid);
			if (permission != null) {
				for (int i = 0; i < permission.length; i++) {
					if (permission[i].equals("android.permission.INTERNET")) {
						if (tx + rx > 0) {
							Log.i(TAG, "Label: " + label /*
														 * + "  Permission: " +
														 * permission[i]
														 */+ "  Total: "
									+ Formatter.formatFileSize(this, tx + rx));
							AppItem item = new AppItem(
									helper.getNewestAppItemId() + 1, label,
									String.valueOf(uid), packageName,
									String.valueOf(System.currentTimeMillis()),
									String.valueOf(System.currentTimeMillis()),
									tx + rx);
							items.add(item);
						}
						break;
					}
					// Log.i(TAG, "Label: " + label + "  Permission: " +
					// permission[i]);
				}
			} else {
				// Log.i(TAG, "Label: " + label + "  Permission: NULL");
			}

			// Log.i(TAG, "label: " + label + " packageName: " + packageName +
			// " up: " + tx + " down" + rx);
		}
		return items;
		// TrafficStats.getMobileTxBytes();//获取手机3g/2g网络上传的总流量
		// TrafficStats.getMobileRxBytes();//手机2g/3g下载的总流量

		// TrafficStats.getTotalTxBytes();//手机全部网络接口 包括wifi，3g、2g上传的总流量
		// TrafficStats.getTotalRxBytes();//手机全部网络接口 包括wifi，3g、2g下载的总流量
	}

	public List<AppItem> getTotalItems() {
		AppItemDbHelper helper = new AppItemDbHelper(this);
		List<AppItem> itemsDB = helper.getAllAppItems();
		List<AppItem> itemCur = getTotal();
		List<AppItem> items = new ArrayList<AppItem>();
		for (int i = 0; i < itemCur.size(); i++) {
			boolean flag = false;
			for (int j = 0; j < itemsDB.size(); j++) {
				if (itemsDB.get(j).getPackagename()
						.equals(itemCur.get(i).getPackagename())) {
					flag = true;
					AppItem item = new AppItem(itemsDB.get(j).getId(), itemsDB
							.get(j).getName(), itemsDB.get(j).getUid(), itemsDB
							.get(j).getPackagename(), itemsDB.get(j)
							.getStarttime(), itemsDB.get(j).getStoptime(),itemsDB.get(j).getUsage()
							+ itemCur.get(i).getUsage());
					items.add(item);
					break;
				}
			}
			if (!flag) {
				items.add(itemCur.get(i));
			}
		}
		return items;
	}

	public void updateToAppItemDB(List<AppItem> items) {
		AppItemDbHelper helper = new AppItemDbHelper(this);
		for (int i = 0; i < items.size(); i++) {
			if (helper.isAppExist(items.get(i).getPackagename())) {
				helper.updateAppItem(items.get(i));
				Log.i(TAG, "DB update");
			} else {
				helper.addAppItem(items.get(i));
				Log.i(TAG, "DB Add");
			}
		}
	}

	public void saveToAppItemDB(String name, String uid, String packagename,
			String starttime, String stoptime, long usage) {
		AppItemDbHelper helper = new AppItemDbHelper(this);
		helper.addAppItem(new AppItem(helper.getNewestAppItemId() + 1, name,
				uid, packagename, starttime, stoptime, usage));
		List<AppItem> items = helper.getAllAppItems();
		for (int i = 0; i < items.size(); i++) {
			Log.i(TAG, "name: " + items.get(i).getName() + "usage: "
					+ items.get(i).getUsage());
		}
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			switch (intent.getAction()) {
			//case CONNECTIVITY_CHANGED:
			//	Log.i(TAG, "CONNECTIVITY_CHANGED:---> " + checkNetworkType());
//
			//	break;
			case SHUT_DOWN:
				Log.i(TAG, "SHUT_DOWN");
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						List<AppItem> itemsTotal = getTotalItems();
						updateToAppItemDB(itemsTotal);
					}
				}).start();
				break;
			case BOOT_COMPLETED:
				Log.i(TAG, "BOOT_COMPLETED");
				break;

			default:
				break;
			}
		}

	};
	
	private int checkNetworkType() {
		ConnectivityManager mConnectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager mTelephony = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		// 检查网络连接
		NetworkInfo info = mConnectivity.getActiveNetworkInfo();
		if (null == info) {
			Log.i(TAG, "null == info");
			return TYPE_NULL;
		}
		int netType = info.getType();
		int netSubtype = info.getSubtype();

		if (netType == ConnectivityManager.TYPE_WIFI) { // WIFI
			if (info.isConnected()) {
				return TYPE_WIFI;
			} else {
				Log.i(TAG, "wifi null");
				return TYPE_NULL;
			}
		} else if (netType == ConnectivityManager.TYPE_MOBILE) { // MOBILE
			if (info.isConnected()) {
				return TYPE_MOBILE;
			} else {
				Log.i(TAG, "mobile null");
				return TYPE_NULL;
			}
		} else {
			Log.i(TAG, "no internet");
			return TYPE_NULL;
		}
	}
}
