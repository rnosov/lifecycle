package com.zchr.util;

import com.authentication.activity.R;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MsgConfirm extends Dialog {

	int dialogResult;
	Handler mHandler;

	public String m_strLeftBtnShow;

	public String m_strRightBtnShow;

	public MsgConfirm(Activity context, String i_strLeftBtnShow, String i_strRightBtnShow) {
		super(context, R.style.dialog_default);
		dialogResult = 0;
		setOwnerActivity(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		m_strLeftBtnShow = i_strLeftBtnShow;
		m_strRightBtnShow = i_strRightBtnShow;

		onCreate();
	}

	public void onCreate() {

		setContentView(R.layout.msg_confirm);

		Button btnLeft = (Button) findViewById(R.id.btn_left);
		Button btnRight = (Button) findViewById(R.id.btn_right);

		btnLeft.setText(m_strLeftBtnShow);
		btnRight.setText(m_strRightBtnShow);

		btnLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				endDialog(0);
			}
		});

		btnRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {
				endDialog(1);
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
}
