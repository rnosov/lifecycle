package com.authentication.activity;


import com.authentication.utils.ToastUtil;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;
import android_serialport_api.CPUAPI;
import android_serialport_api.SerialPortManager;
import android.text.method.ScrollingMovementMethod;

public class CPUActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {
	
	private ProgressDialog progressDialog;
	private static MyApplication application;
	private CPUAPI api;

	private ToggleButton btn0,btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8;
	private Button mBtnInit,mBtnGetRandom;
	private EditText mEdContent;
	private final int CPU_INIT = 0x0;
	private final int CPU_SWITCH = 0x1;
	private final int CPU_CONFREADER = 0x2;
	private final int CPU_CONFPROF = 0x3;
	private final int CPU_SETTING = 0x4;
	private final int CPU_SEARCHCARD = 0x5;
	private final int CPU_COLLCHOOSE = 0x6;
	private final int CPU_CHOOSE = 0x7;
	private final int CPU_RESET = 0x8;
	private final int CPU_GETRANDOM = 0x9;
	private final int CANCEL_PROGRESS = 0x100;
	private final int SHOW_DATA = 0x200;
	private final int SHOW_STATUS = 0x201;
	private final int SHOW_STATUS_SWITCH = 0x202;
	private final int SHOW_STATUS_CONFREADER = 0x203;
	private final int SHOW_STATUS_CONFPROF = 0x204;
	private final int SHOW_STATUS_SETTING = 0x205;
	private final int SHOW_STATUS_SEARCHCARD = 0x206;
	private final int SHOW_STATUS_COLLCHOOSE = 0x207;
	private final int SHOW_STATUS_CHOOSE = 0x208;
	private final int SHOW_STATUS_RESET = 0x209;
	private String retValues = "";
	private String mCardValues ="";
	private boolean mStatus = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cpu_activity);
		initView();
		initData();
	}

	private void initView() {
		btn0 = (ToggleButton) findViewById(R.id.togBtn_0);
		btn1 = (ToggleButton) findViewById(R.id.togBtn_1);
		btn2 = (ToggleButton) findViewById(R.id.togBtn_2);
		btn3 = (ToggleButton) findViewById(R.id.togBtn_3);
		btn4 = (ToggleButton) findViewById(R.id.togBtn_4);
		btn5 = (ToggleButton) findViewById(R.id.togBtn_5);
		btn6 = (ToggleButton) findViewById(R.id.togBtn_6);
		btn7 = (ToggleButton) findViewById(R.id.togBtn_7);
		btn8 = (ToggleButton) findViewById(R.id.togBtn_8);
		btn0.setOnCheckedChangeListener(this);
		btn1.setOnCheckedChangeListener(this);
		btn2.setOnCheckedChangeListener(this);
		btn3.setOnCheckedChangeListener(this);
		btn4.setOnCheckedChangeListener(this);
		btn5.setOnCheckedChangeListener(this);
		btn6.setOnCheckedChangeListener(this);
		btn7.setOnCheckedChangeListener(this);
		btn8.setOnCheckedChangeListener(this);
		
		mBtnInit = (Button)findViewById(R.id.btn_init);
		mBtnGetRandom = (Button)findViewById(R.id.btn_getRandom);
		mBtnInit.setOnClickListener(this);
		mBtnGetRandom.setOnClickListener(this);
		mEdContent = (EditText)findViewById(R.id.ed_content);
	}

	private void initData() {
		application = (MyApplication) this.getApplicationContext();
		Log.i("whw", "initData application=" + application);
		api = new CPUAPI();
	}

	@Override
	public void onClick(View v) {
		boolean isExit = false;
		if (!SerialPortManager.getInstance().isOpen()
				&& !SerialPortManager.getInstance().openSerialPort()) {
			ToastUtil.showToast(this, R.string.open_serial_fail);
			isExit = true;
		}
		if (isExit) {
			return;
		}
		switch(v.getId())
		{
			case R.id.btn_init:
				mCardValues ="";
				mHandler.sendEmptyMessage(CPU_INIT);
				break;
			case R.id.btn_getRandom:
				mHandler.sendEmptyMessage(CPU_GETRANDOM);
				break;
			default:break;
		}
	}
	
	
	private void doInit(){
		//读取切换
		if(!switchStatus())
		{
			mHandler.sendEmptyMessage(CANCEL_PROGRESS);
			return ;
		}
		mHandler.obtainMessage(SHOW_STATUS, SHOW_STATUS_SWITCH,0).sendToTarget();
		//配置读卡器模式
		if(!configurationReaderMode())
		{
			mHandler.sendEmptyMessage(CANCEL_PROGRESS);
			return ;
		}
		mHandler.obtainMessage(SHOW_STATUS, SHOW_STATUS_CONFREADER,0).sendToTarget();
		//配置读卡协议模式
		if(!configurationProtocolMode())
		{
			mHandler.sendEmptyMessage(CANCEL_PROGRESS);
			return ;
		}
		mHandler.obtainMessage(SHOW_STATUS, SHOW_STATUS_CONFPROF,0).sendToTarget();
		//设置读卡器的校验码方式
		if(!setCheckCode())
		{
			mHandler.sendEmptyMessage(CANCEL_PROGRESS);
			return ;
		}
		mHandler.obtainMessage(SHOW_STATUS, SHOW_STATUS_SETTING,0).sendToTarget();
		//寻卡
		if(!findCard())
		{
			mHandler.sendEmptyMessage(CANCEL_PROGRESS);
			return ;
		}
		mHandler.obtainMessage(SHOW_STATUS, SHOW_STATUS_SEARCHCARD,0).sendToTarget();
		//碰撞选卡
		if(!collisionSecectCard())
		{
			mHandler.sendEmptyMessage(CANCEL_PROGRESS);
			return ;
		}
		mHandler.obtainMessage(SHOW_STATUS, SHOW_STATUS_COLLCHOOSE,0).sendToTarget();
		//选卡
		if(!selectCard(mCardValues))
		{
			mHandler.sendEmptyMessage(CANCEL_PROGRESS);
			return ;
		}
		mHandler.obtainMessage(SHOW_STATUS, SHOW_STATUS_CHOOSE,0).sendToTarget();
		//复位
		if(!reset())
		{
			mHandler.sendEmptyMessage(CANCEL_PROGRESS);
			return ;
		}
		mHandler.obtainMessage(SHOW_STATUS, SHOW_STATUS_RESET,0).sendToTarget();
		mHandler.sendEmptyMessage(CANCEL_PROGRESS);
	}
	/**
	 * 
	 */
	private boolean switchStatus()
	{
		//读取切换
		boolean ret = false;
		ret = api.switchStatus() ;
		String tmp ="\r\n" + getResources().getString(R.string.cpu_read_switch);
		if(ret)
		{
			ret = true ;
			tmp += getResources().getString(R.string.cpu_success)+ "\r\n";
		}
		else
			tmp += getResources().getString(R.string.cpu_failure)+ "\r\n";
		retValues += tmp;
		mHandler.obtainMessage(SHOW_DATA, retValues).sendToTarget();
		return ret;
	}
	private boolean configurationReaderMode()
	{
		//配置读卡器模式
		boolean ret = false;
		String retTmp = api.configurationReaderMode();
		String tmp = getResources().getString(R.string.cpu_conf_reader);
		if(retTmp.equals(""))
			tmp += getResources().getString(R.string.cpu_failure)+ "\r\n";
		else
		{
			ret = true;
			tmp += getResources().getString(R.string.cpu_success)+ retTmp ;
		}
		retValues += tmp;
		mHandler.obtainMessage(SHOW_DATA, retValues).sendToTarget();
		return ret;
	}
	private boolean configurationProtocolMode()
	{
		//配置读卡协议模式
		boolean ret = false;
		String retTmp = api.configurationProtocolMode();
		String tmp = getResources().getString(R.string.cpu_conf_prof);
		if(retTmp.equals(""))
			tmp += getResources().getString(R.string.cpu_failure)+ "\r\n";
		else
		{
			ret = true;
			tmp += getResources().getString(R.string.cpu_success)+ retTmp ;
		}
		retValues += tmp;
		mHandler.obtainMessage(SHOW_DATA, retValues).sendToTarget();
		return ret;
	}
	private boolean setCheckCode()
	{
		//设置读卡器的校验码
		boolean ret = false;
		String retTmp = api.setCheckCode();
		String tmp = getResources().getString(R.string.cpu_setting);
		if(retTmp.equals(""))
			tmp += getResources().getString(R.string.cpu_failure)+ "\r\n";
		else
		{
			ret = true;
			tmp += getResources().getString(R.string.cpu_success)+ retTmp ;
		}
		retValues += tmp;
		mHandler.obtainMessage(SHOW_DATA, retValues).sendToTarget();
		return ret;
	}
	private boolean findCard()
	{
		//寻卡
		boolean ret = false;
		String retTmp = api.findCard();
		String tmp = getResources().getString(R.string.cpu_search_card);
		if(retTmp.equals(""))
			tmp += getResources().getString(R.string.cpu_failure)+ "\r\n";
		else
		{
			ret = true;
			tmp += getResources().getString(R.string.cpu_success)+ retTmp + "\r\n";
		}
		retValues += tmp;
		mHandler.obtainMessage(SHOW_DATA, retValues).sendToTarget();
		return ret;	
	}
	private boolean  collisionSecectCard()
	{
		//碰撞选卡
		boolean ret = false;
		String retTmp = api.collisionSecectCard();
		mCardValues = retTmp;
		String tmp = getResources().getString(R.string.cpu_coll_card);
		if(retTmp.equals(""))
			tmp += getResources().getString(R.string.cpu_failure)+ "\r\n";
		else
		{
			ret = true;
			tmp += getResources().getString(R.string.cpu_success)+ retTmp + "\r\n";
		}
		retValues += tmp;
		mHandler.obtainMessage(SHOW_DATA, retValues).sendToTarget();
		return ret;
	}
	private boolean selectCard(String card)
	{
		boolean ret = false;
		String retTmp = api.selectCard(card);
		String tmp = getResources().getString(R.string.cpu_choose_card);
		if(retTmp.equals(""))
			tmp += getResources().getString(R.string.cpu_failure)+ "\r\n";
		else
		{
			ret = true;
			tmp += getResources().getString(R.string.cpu_success)+ retTmp + "\r\n";
		}
		retValues += tmp;
		mHandler.obtainMessage(SHOW_DATA, retValues).sendToTarget();
		return ret ;
	}
	private boolean reset()
	{
		boolean ret = false;
		ret = api.reset();
		String tmp = getResources().getString(R.string.cpu_reset);
		if(!ret)
			tmp += getResources().getString(R.string.cpu_failure)+ "\r\n";
		else
		{
			ret = true;
			tmp += getResources().getString(R.string.cpu_success)+ "\r\n";
		}
		retValues += tmp;
		mHandler.obtainMessage(SHOW_DATA, retValues).sendToTarget();
		return ret ;
	}
	private void getChallenge()
	{
		String retTmp = api.getChallenge();
		String tmp = getResources().getString(R.string.cpu_btn_getrandom);
		if(retTmp.equals(""))
			tmp += getResources().getString(R.string.cpu_failure)+ "\r\n";
		else
		{
			tmp += getResources().getString(R.string.cpu_success)+ retTmp + "\r\n";
		}
		retValues += tmp;
		mHandler.obtainMessage(SHOW_DATA, retValues).sendToTarget();
		mHandler.sendEmptyMessage(CANCEL_PROGRESS);
	}
	@Override
	public void onCheckedChanged(CompoundButton v, boolean checked) {
		switch(v.getId())
		{
			case R.id.togBtn_0:
				if(!mStatus && checked)
					mHandler.sendEmptyMessage(CPU_SWITCH);
				break;
			case R.id.togBtn_1:
				if(!mStatus && checked)
					mHandler.sendEmptyMessage(CPU_CONFREADER);
				break;
			case R.id.togBtn_2:
				if(!mStatus && checked)
					mHandler.sendEmptyMessage(CPU_CONFPROF);
				break;
			case R.id.togBtn_3:
				if(!mStatus && checked)
					mHandler.sendEmptyMessage(CPU_SETTING);
				break;
			case R.id.togBtn_4:
				if(!mStatus && checked)
					mHandler.sendEmptyMessage(CPU_SEARCHCARD);
				break;
			case R.id.togBtn_5:
				if(!mStatus && checked)
					mHandler.sendEmptyMessage(CPU_COLLCHOOSE);
				break;
			case R.id.togBtn_6:
				if(!mStatus && checked)
					mHandler.sendEmptyMessage(CPU_CHOOSE);
				break;
			case R.id.togBtn_7:
				if(!mStatus && checked)
					mHandler.sendEmptyMessage(CPU_RESET);
				break;
			case R.id.togBtn_8://单步操作
				mCardValues ="";
				if(checked)
				{
					btn0.setEnabled(true);
					btn1.setEnabled(true);
					btn2.setEnabled(true);
					btn3.setEnabled(true);
					btn4.setEnabled(true);
					btn5.setEnabled(true);
					btn6.setEnabled(true);
					btn7.setEnabled(true);
					mBtnInit.setEnabled(false);
					mStatus = false;
				}else
				{
					btn0.setEnabled(false);
					btn1.setEnabled(false);
					btn2.setEnabled(false);
					btn3.setEnabled(false);
					btn4.setEnabled(false);
					btn5.setEnabled(false);
					btn6.setEnabled(false);
					btn7.setEnabled(false);
					mBtnInit.setEnabled(true);
					mStatus = true;
				}
				btn0.setChecked(false);
				btn1.setChecked(false);
				btn2.setChecked(false);
				btn3.setChecked(false);
				btn4.setChecked(false);
				btn5.setChecked(false);
				btn6.setChecked(false);
				btn7.setChecked(false);
				break;
			default:break;
		}
		
	}
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case CPU_INIT:
				cancleProgressDialog();
				showProgressDialog(getResources().getString(R.string.cpu_init_progress));
				new Thread(){ 
				     public void run(){ 
				    	 doInit();
				     } 
				}.start(); 
				break;
			case CPU_SWITCH:
				switchStatus();
				break;
			case CPU_CONFREADER:
				//配置读卡器模式
				configurationReaderMode();
				break;
			case CPU_CONFPROF:
				//配置读卡协议模式
				configurationProtocolMode();
				break;
			case CPU_SETTING:
				//设置读卡器的校验码方式
				setCheckCode();
				break;
			case CPU_SEARCHCARD:
				//寻卡
				findCard();
				break;
			case CPU_COLLCHOOSE:
				//碰撞选卡
				collisionSecectCard();
				break;
			case CPU_CHOOSE:
				//选卡
				selectCard(mCardValues);
				break;
			case CPU_RESET:
				//复位
				reset();
				break;
			case CPU_GETRANDOM:
				cancleProgressDialog();
				showProgressDialog(getResources().getString(R.string.cpu_getrandom_progress));
				new Thread(){ 
				     public void run(){ 
				    	 getChallenge();
				     } 
				}.start();
				break;
			case CANCEL_PROGRESS:
				cancleProgressDialog();
				break;
			case SHOW_DATA:
				mEdContent.setText((String)msg.obj);
				mEdContent.setMovementMethod(ScrollingMovementMethod.getInstance());
				mEdContent.setSelection(mEdContent.getText().length(), mEdContent.getText().length());
				break;
			case SHOW_STATUS:
				switch(msg.arg1)
				{
					case SHOW_STATUS_SWITCH:
						btn0.setChecked(true);
						break;
					case SHOW_STATUS_CONFREADER:
						btn1.setChecked(true);
						break;
					case SHOW_STATUS_CONFPROF:
						btn2.setChecked(true);
						break;
					case SHOW_STATUS_SETTING:
						btn3.setChecked(true);
						break;
					case SHOW_STATUS_SEARCHCARD:
						btn4.setChecked(true);
						break;
					case SHOW_STATUS_COLLCHOOSE:
						btn5.setChecked(true);
						break;
					case SHOW_STATUS_CHOOSE:
						btn6.setChecked(true);
						break;
					case SHOW_STATUS_RESET:
						btn7.setChecked(true);
						break;
				}
				break;
			default:
				break;
			}
		}

	};
	
	private void showProgressDialog(String tips) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(tips);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_BACK == keyCode) {
				}
				return false;
			}
		});
		progressDialog.show();
	}

	private void cancleProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		cancleProgressDialog();
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		cancleProgressDialog();
	}
}
