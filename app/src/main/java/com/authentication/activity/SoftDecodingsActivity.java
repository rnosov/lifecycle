package com.authentication.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.annotation.SuppressLint;
import android.app.Activity;
import android_serialport_api.SoftDecodingAPI;
import android_serialport_api.SoftDecodingAPI.IBarCodeData;

/**
 * 4G版A370/CFON640软解码
 * 
 * @author zzd
 * 
 */
public class SoftDecodingsActivity extends Activity implements OnClickListener,
		OnTouchListener, OnCheckedChangeListener, IBarCodeData {
	private Button mBtScan, mBtStartScan, mBtEndScan, mBtClear, mBtSettings;
	private ToggleButton mTbStatus;
	private TextView mTvTotal, mTvSuccess;
	private EditText mEtScanValue, mEtPrefix, mEtSuffix;
	private Spinner mSpOutputMode, mSpTerminalChar, mSpVolume, mSpPlayoneMode;

	private Integer[] volumes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	private String[] outputModes;
	private String[] terminalChars;
	private String[] playoneModes;
	private int totals = 0;
	private int success = 0;

	private Handler mhandler;

	private SoftDecodingAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_soft_decodings);

		init();

		initData();
	}

	private void init() {
		api = new SoftDecodingAPI(getApplication(), this);
		mhandler = new Handler();

		mBtScan = (Button) findViewById(R.id.bt_scan);
		mBtScan.setOnTouchListener(this);

		mBtStartScan = (Button) findViewById(R.id.start_scan);
		mBtStartScan.setOnClickListener(this);

		mBtEndScan = (Button) findViewById(R.id.end_scan);
		mBtEndScan.setOnClickListener(this);

		mBtClear = (Button) findViewById(R.id.clear_scanner_time);
		mBtClear.setOnClickListener(this);

		mBtSettings = (Button) findViewById(R.id.scan_settings);
		mBtSettings.setOnClickListener(this);

		mTbStatus = (ToggleButton) findViewById(R.id.scanner_status);
		mTbStatus.setOnCheckedChangeListener(this);

		mTvTotal = (TextView) findViewById(R.id.total);
		mTvSuccess = (TextView) findViewById(R.id.success_time);

		mEtScanValue = (EditText) findViewById(R.id.scan_value);
		mEtPrefix = (EditText) findViewById(R.id.prefix);
		mEtSuffix = (EditText) findViewById(R.id.suffix);

		mSpOutputMode = (Spinner) findViewById(R.id.output_mode);
		mSpTerminalChar = (Spinner) findViewById(R.id.terminal_char);
		mSpVolume = (Spinner) findViewById(R.id.volume_spinner);
		mSpPlayoneMode = (Spinner) findViewById(R.id.playone_mode_spinner);
	}

	private void initData() {
		outputModes = getResources().getStringArray(R.array.output_mode_array);
		ArrayAdapter<String> outputAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, outputModes);
		mSpOutputMode.setAdapter(outputAdapter);

		terminalChars = getResources().getStringArray(
				R.array.terminal_char_array);
		ArrayAdapter<String> terminalCharAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, terminalChars);
		mSpTerminalChar.setAdapter(terminalCharAdapter);

		ArrayAdapter<Integer> volumeAdapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_spinner_item, volumes);
		mSpVolume.setAdapter(volumeAdapter);

		playoneModes = getResources().getStringArray(R.array.playone_mode);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, playoneModes);
		mSpPlayoneMode.setAdapter(adapter);

		// 读系统的条码设置
		api.getSettings();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_scan:
			api.ContinuousScanning();
			mBtStartScan.setEnabled(false);
			break;
		case R.id.end_scan:
			api.CloseScanning();
			mBtStartScan.setEnabled(true);
			break;
		case R.id.clear_scanner_time:
			mTvTotal.setText("");
			mTvSuccess.setText("");
			mEtScanValue.setText("");
			break;
		case R.id.scan_settings:
			api.setSettings(mTbStatus.isChecked() ? 1 : 0, mSpOutputMode
					.getSelectedItemPosition(), mSpTerminalChar
					.getSelectedItemPosition(), mEtPrefix.getText().toString(),
					mEtSuffix.getText().toString(), volumes[mSpVolume
							.getSelectedItemPosition()], mSpPlayoneMode
							.getSelectedItemPosition());
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			api.setScannerStatus(true);
		} else {
			api.setScannerStatus(false);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			api.scan();
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			api.closeScan();
		}
		return false;
	}

	@Override
	public void sendScan() {
		mhandler.post(new Runnable() {

			@Override
			public void run() {
				totals++;
				mTvTotal.setText(totals + "");
			}
		});
	}

	@Override
	public void onBarCodeData(final String data) {
		mhandler.post(new Runnable() {

			@Override
			public void run() {
				mEtScanValue.setText(data);
				success++;
				mTvSuccess.setText(success + "");
			}
		});
	}

	@Override
	public void getSettings(int PowerOnOff, int OutputMode, int TerminalChar,
			String Prefix, String Suffix, int Volume, int PlayoneMode) {
		mEtPrefix.setText(Prefix);
		mEtSuffix.setText(Suffix);

		if (PowerOnOff == 1) {
			mTbStatus.setChecked(true);
		} else {
			mTbStatus.setChecked(false);
		}

		mSpOutputMode.setSelection(OutputMode);
		mSpTerminalChar.setSelection(TerminalChar);
		mSpVolume.setSelection(Volume);
		mSpPlayoneMode.setSelection(PlayoneMode);
	}

	@Override
	public void setSettingsSuccess() {
		Toast.makeText(getApplicationContext(), "设置成功！", Toast.LENGTH_SHORT)
				.show();
	}
}