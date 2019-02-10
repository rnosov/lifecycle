package com.authentication.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import android_serialport_api.SerialPortManager;

public class BaseUHFActivity extends Activity {
	protected static final int MSG_SHOW_EPC_INFO = 1;
	protected static final int MSG_DISMISS_CONNECT_WAIT_SHOW = 2;
	protected static final int INVENTORY_OVER = 3;
	protected static final int INFO_INV_SUCCESS = 4;
	protected static final int INFO_INV_FAIL = 5;
	protected static final int INFO_STOPINV_SUCCESS = 6;
	protected static final int INFO_STOPINV_FAIL = 7;
	protected static final int INFO_DISCONNECT_SUCCESS = 8;
	protected static final int INFO_DISCONNECT_FAIL = 9;
	protected ToggleButton buttonConnect = null;
	protected ToggleButton buttonInv = null;
	protected ProgressDialog prgDlg = null;
	protected static TaglistFragment objFragment = null;
	protected static TextView txtCount = null;
	Button setting;
	public static TextView txtTimes = null;
	protected static int tagCount = 0;
	protected static int tagTimes = 0;
	protected static List<String> tagInfoList = new ArrayList<String>();
	protected int exitcount = 1;
	protected static HashMap<String, Integer> number = new HashMap<String, Integer>();
	protected static MediaPlayer mediaPlayer = null;
	protected ExecutorService pool;

	@Override
	protected void onResume() {
		pool = Executors.newSingleThreadExecutor();
		mediaPlayer = MediaPlayer.create(this, R.raw.ok);
		super.onResume();
		SerialPortManager.getInstance().openSerialPort();
	}

	@Override
	protected void onPause() {
		mediaPlayer.release();
		mediaPlayer = null;
		pool.shutdown();
		pool = null;
		super.onPause();
		SerialPortManager.getInstance().closeSerialPort();
	}
}
