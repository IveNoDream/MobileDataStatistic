package com.mibound.datastatistic;

import java.text.DecimalFormat;
import java.util.List;

import android.R.integer;
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
import android.text.style.UpdateAppearance;
import android.util.Log;

public class DataStatisticActivity extends Activity {

	private static final String TAG = "mijie";
	private static final String CONNECTIVITY_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";
	private static final String SHUT_DOWN = "android.intent.action.ACTION_SHUTDOWN";
	private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private static final int TYPE_WIFI = 1;
	private static final int TYPE_MOBILE = 2;
	private static final int TYPE_NULL = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_statistic);

		// getTotal();
		registerBroadcast();

	}

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(CONNECTIVITY_CHANGED);
		filter.addAction(BOOT_COMPLETED);
		filter.addAction(SHUT_DOWN);
		registerReceiver(receiver, filter);
	}

	private void getByTrafficStats() {
		/** 获取手机通过 2G/3G 接收的字节流量总数 */
		long mobilerxbytes = TrafficStats.getMobileRxBytes();
		/** 获取手机通过 2G/3G 接收的数据包总数 */
		long mobilerxpackets = TrafficStats.getMobileRxPackets();
		/** 获取手机通过 2G/3G 发出的字节流量总数 */
		long mobiletxbytes = TrafficStats.getMobileTxBytes();
		/** 获取手机通过 2G/3G 发出的数据包总数 */
		long mobiletxpackets = TrafficStats.getMobileTxPackets();
		/** 获取手机通过所有网络方式接收的字节流量总数(包括 wifi) */
		long totalrxbytes = TrafficStats.getTotalRxBytes();
		/** 获取手机通过所有网络方式接收的数据包总数(包括 wifi) */
		long totalrxpackets = TrafficStats.getTotalRxPackets();
		/** 获取手机通过所有网络方式发送的字节流量总数(包括 wifi) */
		long totaltxbytes = TrafficStats.getTotalTxBytes();
		/** 获取手机通过所有网络方式发送的数据包总数(包括 wifi) */
		long totaltxpackets = TrafficStats.getTotalTxPackets();
		/** 获取手机指定 UID 对应的应程序用通过所有网络方式接收的字节流量总数(包括 wifi) */
		// TrafficStats.getUidRxBytes(uid);
		/** 获取手机指定 UID 对应的应用程序通过所有网络方式发送的字节流量总数(包括 wifi) */
		// TrafficStats.getUidTxBytes(uid);

		Log.i(TAG, "mobilerxbytes " + mobilerxbytes + "mobilerxpackets: "
				+ mobilerxpackets + "mobiletxbytes: " + mobiletxbytes);
	}

	private void getTotal() {
		// 1.获取一个包管理器。
		PackageManager pm = getPackageManager();
		// 2.遍历手机操作系统 获取所有的应用程序的uid
		List<ApplicationInfo> appliactaionInfos = pm
				.getInstalledApplications(0);
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
			long tx = TrafficStats.getUidTxBytes(uid);// 发送的 上传的流量byte
			long rx = TrafficStats.getUidRxBytes(uid);// 下载的流量 byte
			// 方法返回值 -1 代表的是应用程序没有产生流量 或者操作系统不支持流量统计
			if (permission != null) {
				for (int i = 0; i < permission.length; i++) {
					if (permission[i].equals("android.permission.INTERNET")) {

						Log.i(TAG, "Label: " + label /*
													 * + "  Permission: " +
													 * permission[i]
													 */+ "  Mobile+WiFi: "
								+ getData(tx + rx));
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
		// TrafficStats.getMobileTxBytes();//获取手机3g/2g网络上传的总流量
		// TrafficStats.getMobileRxBytes();//手机2g/3g下载的总流量

		// TrafficStats.getTotalTxBytes();//手机全部网络接口 包括wifi，3g、2g上传的总流量
		// TrafficStats.getTotalRxBytes();//手机全部网络接口 包括wifi，3g、2g下载的总流量
	}

	private String getData(long data) {
		DecimalFormat df = new DecimalFormat("###.00");
		if (data < 1024) {
			return (data) + "B";
		} else if (data >= 1024 && data < 1024 * 1024) {
			return df.format(data / 1024.0) + "KB";
		} else if (data >= 1024 * 1024 && data < 1024 * 1024 * 1024) {
			return df.format(data / (1024.0 * 1024)) + "MB";
		} else {
			return df.format(data / (1024.0 * 1024 * 1024)) + "GB";
		}
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			switch (intent.getAction()) {
			case CONNECTIVITY_CHANGED:
				Log.i(TAG, "CONNECTIVITY_CHANGED:---> " + checkNetworkType());
				
				break;
			case SHUT_DOWN:
				Log.i(TAG, "SHUT_DOWN");
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
			return TYPE_NULL;
		}
		int netType = info.getType();
		int netSubtype = info.getSubtype();

		if (netType == ConnectivityManager.TYPE_WIFI) { // WIFI
			if (info.isConnected()) {
				return TYPE_WIFI;
			} else {
				return TYPE_NULL;
			}
		} else if (netType == ConnectivityManager.TYPE_MOBILE
				&& netSubtype == TelephonyManager.NETWORK_TYPE_UMTS
				&& !mTelephony.isNetworkRoaming()) { // MOBILE
			if (info.isConnected()) {
				return TYPE_MOBILE;
			} else {
				return TYPE_NULL;
			}
		} else {
			return TYPE_NULL;
		}
	}

	private boolean isMobileTypeAvailable(Context context) {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mMobileNetworkInfo = mConnectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); // 获取移动网络信息
		if (mMobileNetworkInfo != null) {
			return mMobileNetworkInfo.isAvailable(); // getState()方法是查询是否连接了数据网络
		} else {
			return false;
		}
	}

	private boolean isWiFiTypeAvailable(Context context) {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWiFiNetworkInfo = mConnectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI); // 获取移动网络信息
		if (mWiFiNetworkInfo != null) {
			return mWiFiNetworkInfo.isAvailable(); // getState()方法是查询是否连接了数据网络
		} else {
			return false;
		}
	}
}
