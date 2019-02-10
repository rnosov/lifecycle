package com.zchr.util;

import com.authentication.activity.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MsgBox extends Dialog {

	int dialogResult;
	Handler mHandler;

	public MsgBox(Activity context) {
		super(context, R.style.dialog_default);
		dialogResult = 0;
		setOwnerActivity(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		onCreate();
	}

	public void onCreate() {

		setContentView(R.layout.msg_box);
		findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				endDialog(0);
			}
		});
	}

	public int getDialogResult() {
		return dialogResult;
	}

	public void setDialogResult(int dialogResult) {
		this.dialogResult = dialogResult;
	}

	public void endDialog(int result) {
		dismiss();
		setDialogResult(result);
		Message m = mHandler.obtainMessage();
		mHandler.sendMessage(m);
	}

	public int showDialog(String Title, String Msg) {
		TextView TvTitle = (TextView) findViewById(R.id.title);
		TvTitle.setText(Title);
		TextView TvMsgInfo = (TextView) findViewById(R.id.message);
		TvMsgInfo.setText(Msg);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message mesg) {
				throw new RuntimeException();
			}
		};

		super.show();
		try {
			Looper.getMainLooper();
			Looper.loop();
		} catch (RuntimeException e2) {
		}
		return dialogResult;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
