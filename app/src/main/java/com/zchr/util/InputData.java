package com.zchr.util;

import com.authentication.activity.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class InputData extends Dialog {

	public int dialogResult;
	public Handler mHandler;

	public EditText m_editTextInputData;

	public String m_strInputData;

	public int[] m_aiInputCntRange;

	public String m_strDataType;

	public Context m_conTextMain;

	public InputData(Activity context, int[] i_aiCntRange, String i_strDataType) {
		super(context, R.style.dialog_default);
		dialogResult = 0;
		setOwnerActivity(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		m_aiInputCntRange = i_aiCntRange;

		m_conTextMain = context;

		m_strDataType = i_strDataType;

		onCreate();
	}

	public void onCreate() {

		setContentView(R.layout.input_data);

		findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {

				if (m_editTextInputData.getText().toString().length() < m_aiInputCntRange[0] || m_editTextInputData.getText().toString().length() > m_aiInputCntRange[1]) {
					return;
				}

				InputTools.HideKeyboard(m_editTextInputData);

				m_strInputData = m_editTextInputData.getText().toString();

				endDialog(0);
			}
		});

		findViewById(R.id.btn_cancle).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {

				InputTools.HideKeyboard(m_editTextInputData);

				m_strInputData = "";

				endDialog(1);
			}
		});

		m_editTextInputData = (EditText) findViewById(R.id.input_data);

		m_editTextInputData.addTextChangedListener(new MaxLengthWatcher(m_aiInputCntRange[1], m_editTextInputData));

		if (m_strDataType != null) {

			if (m_strDataType.equals("TYPE_CLASS_NUMBER")) {
				m_editTextInputData.setInputType(InputType.TYPE_CLASS_NUMBER);
			} else if (m_strDataType.equals("TYPE_CLASS_PHONE")) {
				m_editTextInputData.setInputType(InputType.TYPE_CLASS_PHONE);
			} else if (m_strDataType.equals("TYPE_PWD")) {
				m_editTextInputData.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			} else if (m_strDataType.equals("TYPE_PIN")) {
				m_editTextInputData.setInputType(InputType.TYPE_CLASS_NUMBER);
				m_editTextInputData.setTransformationMethod(PasswordTransformationMethod.getInstance());
			} else if (m_strDataType.equals("ID_NUM")) {
				m_editTextInputData.setKeyListener(new DigitsKeyListener() {
					@Override
					public int getInputType() {

						return InputType.TYPE_CLASS_NUMBER;
					}

					@Override
					protected char[] getAcceptedChars() {
						char[] data = ((Activity) m_conTextMain).getResources().getString(R.string.login_only_can_input).toCharArray();
						return data;
					}

				});
			}
		}

		InputTools.KeyBoard(m_editTextInputData, "open");
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

	public int showDialog(String Title, ReturnStringData io_returnPin) {
		TextView TvErrorInfo = (TextView) findViewById(R.id.title);
		TvErrorInfo.setText(Title);

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

		io_returnPin.setStringData(m_strInputData);

		return dialogResult;
	}
}
