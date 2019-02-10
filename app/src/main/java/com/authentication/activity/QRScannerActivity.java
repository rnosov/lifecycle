package com.authentication.activity;

import com.authentication.asynctask.AsyncQRScanner;
import com.authentication.utils.ToastUtil;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android_serialport_api.SerialPortManager;

public class QRScannerActivity extends Activity implements OnClickListener,CompoundButton.OnCheckedChangeListener{
	private EditText etShowInfo;
	private EditText etTime;
	private EditText etModeTime;
	private CheckBox cbQR;
	private CheckBox cbDM;
	private CheckBox cbBarcode;
	private CheckBox cbNFC;
	private RadioGroup rgModel;
	private RadioButton rbNormal;
	private RadioButton rbOnce;
	private RadioButton rbInterval;
	private Button btnComfirm;
	private Button btnQuery;
	private Button btnOpen;
	private Button btnClose;
	
	private byte[] code;
	private InnerThread thread;
	private boolean isRunning = false;
	private AsyncQRScanner asyncQRScanner;
	private MyApplication application;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AsyncQRScanner.QUERY_DEVICE_INFO:
				boolean isOk = (Boolean) msg.obj;
				if(isOk){
					etShowInfo.setText("设备正常！\n"+etShowInfo.getText().toString());
				}else{
					etShowInfo.setText("设备不正常！\n"+etShowInfo.getText().toString());
				}
				break;
			case AsyncQRScanner.SET_MODE:
				boolean mode = (Boolean) msg.obj;
				if(mode){
					etShowInfo.setText("设置成功！\n"+etShowInfo.getText().toString());
				}else{
					etShowInfo.setText("设备失败！\n"+etShowInfo.getText().toString());
				}
				break;
			case AsyncQRScanner.READ_CODE:
				code = (byte[]) msg.obj;
				if(code!=null){
					showCode(code);
				}
				break;

			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrscanner);
		SerialPortManager.getInstance().openSerialPort();
		initView();
		if(SerialPortManager.getInstance().getBaudrate()!=115200){
			etShowInfo.setText("波特率不正确！\n"+etShowInfo.getText().toString());
		}
		setListeners();
	}

	private void setListeners() {
		rgModel.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.qr_check_normal:
					Log.d("jokey", "normal");
					asyncQRScanner.setNormalMode();
					break;
				case R.id.qr_check_once:
					Log.d("jokey", "once");
					asyncQRScanner.setOnceMode();
					break;
				case R.id.qr_check_interval:
					Log.d("jokey", "interval");
					String str = etModeTime.getText().toString();
					if(!TextUtils.isEmpty(str)){
						int time = Integer.parseInt(str);
						if(time>0&&time<=10){
							asyncQRScanner.setIntervalMode(time);
						}else{
							ToastUtil.showToast(QRScannerActivity.this, "超出范围！（1~10）");
						}
					}else{
						ToastUtil.showToast(QRScannerActivity.this, "间隔时间不能为空！");
					}
					break;
				}
			}
		});
	}

	private void initView() {
		etShowInfo = (EditText) findViewById(R.id.qr_show_code);
		etTime = (EditText) findViewById(R.id.qr_time_interval);
		etModeTime = (EditText) findViewById(R.id.qr_mode_interval_time);
		
		cbQR = (CheckBox) findViewById(R.id.qr_check_qr);
		cbQR.setOnCheckedChangeListener(this);
		cbDM = (CheckBox) findViewById(R.id.qr_check_dm);
		cbDM.setOnCheckedChangeListener(this);
		cbBarcode = (CheckBox) findViewById(R.id.qr_check_barcode);
		cbBarcode.setOnCheckedChangeListener(this);
		cbNFC = (CheckBox) findViewById(R.id.qr_check_nfc);
		cbNFC.setOnCheckedChangeListener(this);
		
		rbNormal = (RadioButton) findViewById(R.id.qr_check_normal);
		rbOnce = (RadioButton) findViewById(R.id.qr_check_once);
		rbInterval = (RadioButton) findViewById(R.id.qr_check_interval);
		rgModel = (RadioGroup) findViewById(R.id.qr_check_model);
		
		btnComfirm = (Button) findViewById(R.id.qr_time_interval_confirm);
		btnComfirm.setOnClickListener(this);
		btnQuery = (Button) findViewById(R.id.qr_query_info);
		btnQuery.setOnClickListener(this);
		btnOpen = (Button) findViewById(R.id.qr_open);
		btnOpen.setOnClickListener(this);
		btnClose = (Button) findViewById(R.id.qr_close);
		btnClose.setOnClickListener(this);
		
		application = (MyApplication) this.getApplicationContext();
		asyncQRScanner = new AsyncQRScanner(application.getHandlerThread().getLooper(), mHandler);
	}
	/**
	 * 显示扫到的条码
	 * @param code
	 */
	private void showCode(byte[] code){
		String QRcode = "";
		for (int i = 6; i < code.length-1; i++) {
			QRcode += (char)code[i]+"";
		}
		etShowInfo.setText(QRcode+"\n"+etShowInfo.getText().toString());
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.qr_time_interval_confirm:
			String str = etTime.getText().toString();
			if(!TextUtils.isEmpty(str)){
				long time = Long.parseLong(str);
				if(time>=0&&time<=60000){
					asyncQRScanner.updataIntervalTime(time);
				}else{
					ToastUtil.showToast(this, "超出范围！（0~60000）");
				}
			}else{
				ToastUtil.showToast(this, "间隔时间不能为空！");
			}
			break;
		case R.id.qr_query_info:
			asyncQRScanner.queryDeviceInfo();
			break;
		case R.id.qr_open:
			setEnable(false);
			isRunning = true;
			startRead();
			btnClose.setEnabled(true);
			break;
		case R.id.qr_close:
			setEnable(true);
			isRunning = false;
			stopRead();
			btnClose.setEnabled(false);
			break;
		}
	}
	@Override
	protected void onDestroy() {
		isRunning = false;
		stopRead();
		SerialPortManager.getInstance().closeSerialPort();
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.qr_check_qr:
			asyncQRScanner.setType(checkStatus());
			break;
		case R.id.qr_check_dm:
			asyncQRScanner.setType(checkStatus());
			break;
		case R.id.qr_check_barcode:
			asyncQRScanner.setType(checkStatus());
			break;
		case R.id.qr_check_nfc:
			asyncQRScanner.setType(checkStatus());
			break;
		}
	}
	private int checkStatus(){
		String qr = "0";
		String dm = "0";
		String barcode = "0";
		String nfc = "0";
		if(cbQR.isChecked()){
			qr = "1";
		}
		if(cbDM.isChecked()){
			dm = "1";
		}
		if(cbBarcode.isChecked()){
			barcode = "1";
		}
		if(cbNFC.isChecked()){
			nfc = "1";
		}
		String sum = nfc+barcode+dm+qr;
		return Integer.valueOf(sum, 2);
	}
	private void setEnable(boolean enable){
		cbQR.setEnabled(enable);
		cbDM.setEnabled(enable);
		cbBarcode.setEnabled(enable);
		cbNFC.setEnabled(enable);
		
		rbNormal.setEnabled(enable);
		rbOnce.setEnabled(enable);
		rbInterval.setEnabled(enable);
		
		btnComfirm.setEnabled(enable);
		btnQuery.setEnabled(enable);
		btnOpen.setEnabled(enable);
	}

	
	private void startRead(){
		if(thread==null){
			thread = new InnerThread();
			isRunning = true;
		}
		thread.start();
	}
	
	private void stopRead(){
		if(thread!=null){
			thread = null;
			isRunning = false;
		}
	}
	
	private class InnerThread extends Thread{
		@Override
		public void run() {
			while(isRunning){
//				Log.d("jokey", "isRunning:  "+isRunning);
				asyncQRScanner.readCode();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
