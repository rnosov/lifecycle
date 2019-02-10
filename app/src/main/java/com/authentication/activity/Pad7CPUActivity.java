package com.authentication.activity;

import com.authentication.utils.DataUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.Pad7CPUAPI;
import android_serialport_api.Pad7PsamAPI.OnA370ResetListener;
import android_serialport_api.Pad7PsamAPI.OnA370SendListener;

public class Pad7CPUActivity extends Activity implements OnClickListener{
	private Pad7CPUAPI api;
	private EditText mEtCMD;
	private TextView mTvReceiveData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pda7_cpu);

		api = new Pad7CPUAPI();
		api.open();

		Button mBtReset = (Button) findViewById(R.id.bt_pad7_reset);
		mBtReset.setOnClickListener(this);

		Button mBtGetRandom = (Button) findViewById(R.id.bt_pad7_get_random);
		mBtGetRandom.setOnClickListener(this);

		Button mBtSend = (Button) findViewById(R.id.bt_pad7_send);
		mBtSend.setOnClickListener(this);

		mEtCMD = (EditText) findViewById(R.id.et_pad7_cmd);
		mTvReceiveData = (TextView) findViewById(R.id.tv_pad7_receive_data);
	}

	@Override
	protected void onDestroy() {
		api.close();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_pad7_reset:
			api.reset(new OnA370ResetListener() {

				@Override
				public void resetSuccess(byte[] receiveData) {
					Toast.makeText(getApplicationContext(), "reset success", Toast.LENGTH_SHORT).show();
				}

				@Override
				public void resetFailure(int code) {
					Toast.makeText(getApplicationContext(), "reset failure", Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case R.id.bt_pad7_get_random:
			api.getRandom(new OnA370SendListener() {

				@Override
				public void receiveData(byte[] receiveData) {
					mTvReceiveData.setText(DataUtils.toHexString(receiveData));
				}

				@Override
				public void failure() {
					Toast.makeText(getApplicationContext(), "return failure", Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case R.id.bt_pad7_send:
			api.send(DataUtils.hexStringTobyte(mEtCMD.getText().toString()), new OnA370SendListener() {

				@Override
				public void receiveData(byte[] receiveData) {
					mTvReceiveData.setText(DataUtils.toHexString(receiveData));
				}

				@Override
				public void failure() {
					Toast.makeText(getApplicationContext(), "return failure", Toast.LENGTH_SHORT).show();
				}
			});
			break;
		}
	}
}