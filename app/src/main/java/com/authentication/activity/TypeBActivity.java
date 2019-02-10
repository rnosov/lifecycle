package com.authentication.activity;

import com.authentication.utils.ToastUtil;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android_serialport_api.SerialPortManager;
import android_serialport_api.TypeBCardAPI;

public class TypeBActivity extends BaseActivity implements OnClickListener {
	private Button release;
	private Button use;

	TypeBCardAPI api;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.type_b_activity);
		initView();
		initData();
	}

	private void initView() {
		release = (Button) findViewById(R.id.release);
		use = (Button) findViewById(R.id.use);

		release.setOnClickListener(this);
		use.setOnClickListener(this);
	}

	private void initData() {
		api = new TypeBCardAPI();
	}

	@Override
	public void onClick(View view) {
		boolean isExit = false;
		if (!SerialPortManager.getInstance().isOpen()
				&& !SerialPortManager.getInstance().openSerialPort()) {
			ToastUtil.showToast(this, R.string.open_serial_fail);
			isExit = true;
		}
		if(isExit){
			return;
		}
		int id = view.getId();
		switch (id) {
		case R.id.release:
			api.release('1');
			break;
		case R.id.use:
			api.comsume('2');
			break;

		default:
			break;
		}

	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}

}
