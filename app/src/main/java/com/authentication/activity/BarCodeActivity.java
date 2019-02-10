package com.authentication.activity;

import com.authentication.asynctask.AsyncBarCode;
import com.authentication.asynctask.AsyncBarCode.OnOpenListener;
import com.authentication.asynctask.AsyncBarCode.OnCloseListener;
import com.authentication.asynctask.AsyncBarCode.OnPrepareListener;
import com.authentication.asynctask.AsyncBarCode.OnStartDecodeListener;
import com.authentication.utils.ToastUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android_serialport_api.SerialPortManager;
import android_serialport_api.BarCodeAPI;

public class BarCodeActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener {

	private MyApplication application;
	protected HandlerThread handlerThread;
	private Button scan_button;

	private Button setting_button;

	private Button loop_scan;

	private Button stop_loop;

	private TextView loopInfoView;

	private TextView receiveDataView;

	private ProgressDialog progressDialog;

	private AsyncBarCode asyncBarCode;

	MediaPlayer mediaPlayer = null;

	private SharedPreferences preferences;

	private boolean isOpen;
	private boolean isInit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.barcode_activity);
		application = (MyApplication) this.getApplicationContext();
		handlerThread = application.getHandlerThread();
		initView();
		initData();
	}

	private void initView() {
		loopInfoView = (TextView) findViewById(R.id.loopInfo);
		receiveDataView = (TextView) findViewById(R.id.receiveInfo);
		scan_button = (Button) findViewById(R.id.scan);
		setting_button = (Button) findViewById(R.id.setting_button);
		loop_scan = (Button) findViewById(R.id.loop_scan);
		stop_loop = (Button) findViewById(R.id.stop_loop);

		scan_button.setOnClickListener(this);
		setting_button.setOnClickListener(this);
		loop_scan.setOnClickListener(this);
		stop_loop.setOnClickListener(this);
	}

	private void initData() {
		mediaPlayer = MediaPlayer.create(this, R.raw.barcode);
		preferences = getSharedPreferences("init", MODE_PRIVATE);

		asyncBarCode = new AsyncBarCode(handlerThread.getLooper());
		boolean isExit = false;
		if (!SerialPortManager.getInstance().isOpen()
				&& !SerialPortManager.getInstance().openSerialPort()) {
			ToastUtil.showToast(this, R.string.open_serial_fail);
			isExit = true;
		}

		if (isExit) {
			return;
		}
		asyncBarCode.setOnOpenListener(new OnOpenListener() {

			@Override
			public void onOpenSuccess() {
				ToastUtil
						.showToast(BarCodeActivity.this, R.string.open_success);
				isOpen = true;
				cancleProgressDialog();
				init();
			}

			@Override
			public void onOpenFail() {
				ToastUtil.showToast(BarCodeActivity.this, R.string.open_fail);
				cancleProgressDialog();
				isOpen = false;
			}
		});

		asyncBarCode.setOnCloseListener(new OnCloseListener() {

			@Override
			public void onCloseSuccess() {
				ToastUtil.showToast(BarCodeActivity.this,
						R.string.close_success);
				if (SerialPortManager.getInstance().isOpen()) {
					SerialPortManager.getInstance().closeSerialPort();
				}
			}

			@Override
			public void onCloseFail() {
				ToastUtil.showToast(BarCodeActivity.this, R.string.close_fail);
				if (SerialPortManager.getInstance().isOpen()) {
					SerialPortManager.getInstance().closeSerialPort();
				}
			}
		});

		asyncBarCode.setOnPrepareListener(new OnPrepareListener() {
			@Override
			public void OnPrepare(boolean initSuccess) {
				cancleProgressDialog();
				isInit = initSuccess;
				Editor editor = preferences.edit();
				editor.putBoolean("isInit", isInit);
				editor.commit();
			}

		});

		asyncBarCode.setOnStartDecodeListener(new OnStartDecodeListener() {

			@Override
			public void OnDecodeSuccess(byte[] data) {
				refresh(data);
				if (!isLoop) {
					return;
				} else {
					setLoopInfo(true);
					mHandler.post(task);
				}
			}

			@Override
			public void OnDecodeFail() {
				ToastUtil.showToast(BarCodeActivity.this, R.string.decode_fail);
				if (!isLoop) {
					return;
				} else {
					setLoopInfo(false);
					mHandler.post(task);
				}

			}
		});

		asyncBarCode.open();
		showProgressDialog(getString(R.string.open_sanner));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isonPause) {
			isonPause = false;
		}
		if (!isStop) {
			return;
		}
		init();
	}

	private void init() {
		isInit = preferences.getBoolean("isInit", false);
		Log.i("whw", "isInit=" + isInit);
		if (!isInit) {
			showProgressDialog(getString(R.string.prepare_dialog));
			asyncBarCode.prepareDecode();
		}
	}

	private boolean isStop;

	private boolean isonPause;

	@Override
	protected void onPause() {
		super.onPause();
		isonPause = true;
		isLoop = false;
		Log.i("whw", "onPause");
	}

	@Override
	protected void onStop() {
		Log.i("whw", "onStop");
		super.onStop();
		isStop = true;
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();
		switch (id) {
		case R.id.scan:
			scan();
			break;
		case R.id.setting_button:
			if (!isInit) {
				ToastUtil.showToast(this, R.string.not_init);
				return;
			}
			startActivity(new Intent(this, BarCodeSettingActivity.class));
			break;
		case R.id.loop_scan:
			isLoop = true;
			total = 0;
			success = 0;
			fail = 0;
			mHandler.post(task);
			break;
		case R.id.stop_loop:
			isLoop = false;
			total = 0;
			success = 0;
			fail = 0;
			break;
		default:
			break;
		}

	}

	private void scan() {
		if (isonPause) {
			return;
		}
		if (!isOpen) {
			ToastUtil.showToast(this, R.string.not_open);
			return;
		}
		if (!isInit) {
			ToastUtil.showToast(this, R.string.not_init);
			return;
		}

		receiveDataView.setText("");
		asyncBarCode.startDecode();
	}

	private Runnable task = new Runnable() {

		@Override
		public void run() {
			scan();
		}
	};

	private boolean isLoop = false;

	private int total = 0;
	private int success = 0;
	private int fail = 0;

	private void setLoopInfo(boolean isSuccess) {
		total++;
		if (isSuccess) {
			success++;
		} else {
			fail++;
		}
		loopInfoView.setText("Total:" + total + "\nSuccess:" + success
				+ "\nFail:" + fail);
	}

	private Handler mHandler = new Handler();

	private void refresh(byte[] data) {
		if (isonPause || mediaPlayer == null) {
			return;
		}
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.seekTo(0);
		} else {
			mediaPlayer.start();
		}
		String dataStr = "";
		if (data != null) {
			dataStr = new String(data);
		}
		receiveDataView.setText(dataStr);
	}

	@Override
	protected void onDestroy() {
		cancleProgressDialog();
		mediaPlayer.release();
		mediaPlayer = null;
		Log.i("whw", "onDestroy  =" + this.toString());
		asyncBarCode.close();
		handlerThread = null;
		super.onDestroy();
	}

	private void showProgressDialog(String message) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMessage(message);
		if (!progressDialog.isShowing()) {
			progressDialog.show();
		}
	}

	private void cancleProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int groupId = group.getId();
		switch (groupId) {
		case R.id.pdf417:
			if (R.id.enable_pdf417 == checkedId) {
				asyncBarCode.setParameter(BarCodeAPI.PDF417_ENABLE);
				Log.i("whw", "setEnablePDF417(true)");
			} else {
				asyncBarCode.setParameter(BarCodeAPI.PDF417_DISABLE);
				Log.i("whw", "setEnablePDF417(false)");
			}
			break;
		case R.id.micro_pdf417:
			if (R.id.enable_micro_pdf417 == checkedId) {
				asyncBarCode.setParameter(BarCodeAPI.MICRO_PDF417_ENABLE);
				Log.i("whw", "setEnableMicroPDF417(true)");
			} else {
				asyncBarCode.setParameter(BarCodeAPI.MICRO_PDF417_DISABLE);
				Log.i("whw", "setEnableMicroPDF417(false)");
			}
			break;
		case R.id.data_matrix:
			if (R.id.enable_data_matrix == checkedId) {
				asyncBarCode.setParameter(BarCodeAPI.DATA_MATRIX_ENABLE);
				Log.i("whw", "setEnableDataMatrix(true)");
			} else {
				asyncBarCode.setParameter(BarCodeAPI.DATA_MATRIX_DISABLE);
				Log.i("whw", "setEnableDataMatrix(false)");
			}
			break;
		case R.id.maxicode:
			if (R.id.enable_maxicode == checkedId) {
				asyncBarCode.setParameter(BarCodeAPI.MAXICODE_ENABLE);
				Log.i("whw", "setEnableMaxicode(true)");
			} else {
				asyncBarCode.setParameter(BarCodeAPI.MAXICODE_DISABLE);
				Log.i("whw", "setEnableMaxicode(false)");
			}
			break;
		case R.id.qrcode:
			if (R.id.enable_qr_code == checkedId) {
				asyncBarCode.setParameter(BarCodeAPI.QR_CODE_ENABLE);
				Log.i("whw", "setEnableQRCode(true)");
			} else {
				asyncBarCode.setParameter(BarCodeAPI.QR_CODE_DISABLE);
				Log.i("whw", "setEnableQRCode(false)");
			}
			break;
		case R.id.micro_qr:
			if (R.id.enable_micro_qr == checkedId) {
				asyncBarCode.setParameter(BarCodeAPI.MICRO_QR_ENABLE);
				Log.i("whw", "setEnableMicroQR(true)");
			} else {
				asyncBarCode.setParameter(BarCodeAPI.MICRO_QR_DISABLE);
				Log.i("whw", "setEnableMicroQR(false)");
			}
			break;
		case R.id.aztec:
			if (R.id.enable_aztec == checkedId) {
				asyncBarCode.setParameter(BarCodeAPI.AZTEC_ENABLE);
				Log.i("whw", "setEnableAztec(true)");
			} else {
				asyncBarCode.setParameter(BarCodeAPI.AZTEC_DISABLE);
				Log.i("whw", "setEnableAztec(false)");
			}
			break;

		default:
			break;
		}
	}
}