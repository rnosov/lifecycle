package com.authentication.asynctask;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android_serialport_api.QRScannerAPI;

public class AsyncQRScanner {
	public static final int QUERY_DEVICE_INFO = 1;
	public static final int UPDATE_INTERVAL_TIME = 2;
	public static final int NORMAL_MODE = 3;
	public static final int ONCE_MODE = 4;
	public static final int INTERVAL_MODE = 5;
	public static final int SET_MODE = 6;
	public static final int SET_SCAN_TYPE = 7;
	public static final int READ_CODE = 8;
	private Handler mHandler;
	private WorkHandler mWorkHandler;
	private QRScannerAPI api;
	
	public AsyncQRScanner(Looper looper,Handler mHandler) {
		this.mHandler = mHandler;
		this.mWorkHandler = new WorkHandler(looper);
		api = new QRScannerAPI();
	}
	private class WorkHandler extends Handler{
		public WorkHandler(Looper looper) {
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case QUERY_DEVICE_INFO:
				boolean device = api.queryDeviceInfo();
				mHandler.obtainMessage(QUERY_DEVICE_INFO, device).sendToTarget();
				break;
			case UPDATE_INTERVAL_TIME:
				long time = (Long) msg.obj;
				boolean update= api.updataIntervalTime(time);
				mHandler.obtainMessage(SET_MODE, update).sendToTarget();
				break;
			case NORMAL_MODE:
				boolean normal = api.setNormal();
				mHandler.obtainMessage(SET_MODE, normal).sendToTarget();
				break;
			case ONCE_MODE:
				boolean once = api.setOnce();
				mHandler.obtainMessage(SET_MODE, once).sendToTarget();
				break;
			case INTERVAL_MODE:
				int second = (Integer) msg.obj;
				boolean interval = api.setInterval(second);
				mHandler.obtainMessage(SET_MODE, interval).sendToTarget();
				break;
			case SET_SCAN_TYPE:
				int type = (Integer) msg.obj;
				boolean scanType = api.setScanType(type);
				mHandler.obtainMessage(SET_MODE, scanType).sendToTarget();
				break;
			case READ_CODE:
				byte[] code = api.readCode();
				if(code!=null){
					mHandler.obtainMessage(READ_CODE, code).sendToTarget();
				}
				break;
			}
		}
	}
	/**
	 * 获取设备信息
	 */
	public void queryDeviceInfo(){
		mWorkHandler.sendEmptyMessage(QUERY_DEVICE_INFO);
	}
	/**
	 * 设置普通模式
	 */
	public void setNormalMode(){
		mWorkHandler.sendEmptyMessage(NORMAL_MODE);
	}
	/**
	 * 获取单次模式
	 */
	public void setOnceMode(){
		mWorkHandler.sendEmptyMessage(ONCE_MODE);
	}
	/**
	 * 获取间隔模式
	 */
	public void setIntervalMode(int second){
		mWorkHandler.obtainMessage(INTERVAL_MODE,second).sendToTarget();
	}
	/**
	 * 修改间隔扫描时间
	 * @param time  0~60000 单位(ms)
	 */
	public void updataIntervalTime(long time){
		mWorkHandler.obtainMessage(UPDATE_INTERVAL_TIME, time).sendToTarget();
	}
	/**
	 * 修改扫描类型
	 * @param type  类型
	 */
	public void setType(int type){
		mWorkHandler.obtainMessage(SET_SCAN_TYPE, type).sendToTarget();
	}
	/**
	 * 修改扫描类型
	 * @param type  类型
	 */
	public void readCode(){
		mWorkHandler.sendEmptyMessage(READ_CODE);
	}
}
