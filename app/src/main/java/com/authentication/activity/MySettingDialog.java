package com.authentication.activity;

import com.authentication.utils.ToastUtil;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MySettingDialog extends Dialog implements
		android.view.View.OnClickListener {
	private Spinner mSpArea;
	private Button btn_ok;
	private Button btn_cancel;
	private EditText mEtTimeout, mEtPwd, mEtOffSet, mEtLength;
	private OnMySettingCallback callback;
	private Context context;

	public MySettingDialog(Context context) {
		super(context);
		this.context = context;
	}

	public void setOnMySettingCallback(OnMySettingCallback callback) {
		this.callback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_dialog_hx);
		setTitle(R.string.txt_singleScanSetting);

		init();

		setCancelable(false);
	}

	private void init() {
		mSpArea = (Spinner) findViewById(R.id.sp_area);
		String[] areas = new String[] { "EPC", "TID", "USER" };

		ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(context,
				R.layout.simple_list_item, areas);
		areaAdapter.setDropDownViewResource(R.layout.simple_list_item);
		mSpArea.setAdapter(areaAdapter);

		mEtTimeout = (EditText) findViewById(R.id.et_outtime);
		mEtPwd = (EditText) findViewById(R.id.et_pwd);
		mEtOffSet = (EditText) findViewById(R.id.et_offset);
		mEtLength = (EditText) findViewById(R.id.et_strlength);

		btn_ok = (Button) findViewById(R.id.ok);
		btn_ok.setOnClickListener(this);

		btn_cancel = (Button) findViewById(R.id.cancel);
		btn_cancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			if (mEtLength.getText().toString().equals("")
					|| Integer.parseInt(mEtLength.getText().toString()) == 0) {
				ToastUtil.showToast(context, "数据长度必须大于0");
			} else {
				callback.onSetting(Integer.parseInt(mEtTimeout.getText()
						.toString()), mSpArea.getSelectedItemPosition(), mEtPwd
						.getText().toString(), Integer.parseInt(mEtOffSet
						.getText().toString()), Integer.parseInt(mEtLength
						.getText().toString()));
				dismiss();
			}
			break;
		case R.id.cancel:
			dismiss();
			break;
		default:
			break;
		}
	}

	public interface OnMySettingCallback {
		void onSetting(int times, int code, String pwd, int sa, int dl);
	};
}