package com.authentication.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.authentication.utils.DataUtils;
import com.authentication.utils.ToastUtil;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.Activity;
import android_serialport_api.PsamAPI;
import android_serialport_api.PsamAPI.OnPsamCallback;
import android_serialport_api.SerialPortManager;

public class PsamActivity extends Activity implements OnClickListener,
		OnPsamCallback, OnItemSelectedListener {
	private Button mBtOpen, mBtReset, mBtGetRandom, mBtDown, mBtClose, mBtSend;
	private EditText mEtSend;
	private Spinner mSpSam;
	private TextView mTvCustomData;
	private PsamAPI api;
	private String[] list = { "请选择sam卡", "sam卡1", "sam卡2" };
	private ArrayAdapter<String> adapter;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_psam);

		SerialPortManager.getInstance().openSerialPort();

		init();
	}

	private void init() {
		api = new PsamAPI(getApplicationContext(), this);
		handler = new Handler();

		mSpSam = (Spinner) findViewById(R.id.sp_sam);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpSam.setAdapter(adapter);
		mSpSam.setOnItemSelectedListener(this);

		mEtSend = (EditText) findViewById(R.id.et_send);
		mTvCustomData = (TextView) findViewById(R.id.tv_customData);

		mBtOpen = (Button) findViewById(R.id.bt_open);
		mBtOpen.setOnClickListener(this);

		mBtReset = (Button) findViewById(R.id.bt_reset);
		mBtReset.setOnClickListener(this);

		mBtSend = (Button) findViewById(R.id.bt_send);
		mBtSend.setOnClickListener(this);

		mBtGetRandom = (Button) findViewById(R.id.bt_getRandom);
		mBtGetRandom.setOnClickListener(this);

		mBtDown = (Button) findViewById(R.id.bt_down);
		mBtDown.setOnClickListener(this);

		mBtClose = (Button) findViewById(R.id.bt_close);
		mBtClose.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_open:
			handler.post(new Runnable() {

				@Override
				public void run() {
					api.open();
				}
			});
			break;
		case R.id.bt_reset:
			handler.post(new Runnable() {

				@Override
				public void run() {
					api.Reset();
				}
			});
			break;
		case R.id.bt_send:
			if (check(mEtSend.getText().toString())) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						try {
							api.Custom(mEtSend.getText().toString());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
			} else {
				ToastUtil.showToast(getApplicationContext(), "该数据不是16进制数据");
			}
			break;
		case R.id.bt_getRandom:
			handler.post(new Runnable() {

				@Override
				public void run() {
					try {
						api.Custom("0084000008");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			break;
		case R.id.bt_down:
			handler.post(new Runnable() {

				@Override
				public void run() {
					api.Down();
				}
			});
			break;
		case R.id.bt_close:
			handler.post(new Runnable() {

				@Override
				public void run() {
					api.close();
				}
			});
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 1:
			handler.post(new Runnable() {

				@Override
				public void run() {
					api.SELECT0();
				}
			});
			break;
		case 2:
			handler.post(new Runnable() {

				@Override
				public void run() {
					api.SELECT1();
				}
			});
			break;
		default:
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private boolean check(String data) {
		Matcher m = Pattern.compile("^[0-9A-Fa-f]$").matcher(data);
		return m.matches();
	}

	@Override
	protected void onDestroy() {
		api.close();
		SerialPortManager.getInstance().closeSerialPort();
		super.onDestroy();
	}

	@Override
	public void OnOpenSuccess() {
		ToastUtil.showToast(getApplicationContext(), "打开模块成功");
	}

	@Override
	public void OnOpenFailed() {
		ToastUtil.showToast(getApplicationContext(), "打开模块失败");
	}

	@Override
	public void OnResetSuccess() {
		ToastUtil.showToast(getApplicationContext(), "复位成功");
	}

	@Override
	public void OnResetFailed() {
		ToastUtil.showToast(getApplicationContext(), "复位失败");
	}

	@Override
	public void OnSelectSam0Success() {
		ToastUtil.showToast(getApplicationContext(), "选择sam卡1成功");
	}

	@Override
	public void OnSelectSam0Failed() {
		ToastUtil.showToast(getApplicationContext(), "选择sam卡1失败");
	}

	@Override
	public void OnSelectSam1Success() {
		ToastUtil.showToast(getApplicationContext(), "选择sam卡2成功");
	}

	@Override
	public void OnSelectSam1Failed() {
		ToastUtil.showToast(getApplicationContext(), "选择sam卡2失败");
	}

	@Override
	public void OnCustomSamSuccess(byte[] data) {
		ToastUtil.showToast(getApplicationContext(), "自定义指令成功");
		mTvCustomData.setText("指令返回数据：" + DataUtils.toHexString(data));
	}

	@Override
	public void OnCustomSamFailed() {
		ToastUtil.showToast(getApplicationContext(), "自定义指令失败");
	}
}