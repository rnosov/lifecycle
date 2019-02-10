package com.authentication.activity;

import com.authentication.asynctask.AsyncM1Card;
import com.authentication.asynctask.AsyncM1Card.OnReadAtPositionListener;
import com.authentication.asynctask.AsyncM1Card.OnReadCardNumListener;
import com.authentication.asynctask.AsyncM1Card.OnUpdatePwdListener;
import com.authentication.asynctask.AsyncM1Card.OnWriteAtPositionListener;
import com.authentication.utils.DataUtils;
import com.authentication.utils.RegexUtils;
import com.authentication.utils.ToastUtil;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android_serialport_api.M1CardAPI;
import android_serialport_api.SerialPortManager;

/**
 * M1卡
 */
public class RFIDActivity extends BaseActivity implements OnClickListener {

	private Spinner mSpinnerCardType, mSpinnerPwdType;
	private ArrayAdapter<String> mAdapterCardType, mAdapterPwdType;
	private static final String[] cardtype = { "S50", "S70" };
	private static final String[] pwdtype = { "KEYA", "KEYB" };
	private static final int[] keyType = { M1CardAPI.KEY_A, M1CardAPI.KEY_B };
	private static int NUM = 1;
	private static int mKeyType = M1CardAPI.KEY_A;
	private String DefaultKeyA = "ffffffffffff";// 默认密码A
	private String DefaultKeyB = "ffffffffffff";// 默认密码B
	private Button mBtnGetCardNum, mBtnSendPwd, mBtnValidPwd, mBtnWriteData,
			mBtnReadData, mBtnUpdate;
	private EditText mEdShowCard, mEdPwdA, mEdPwdB, mBlockNum, mEdWriteData,
			mReadData, mEdWritePwd;
	private TextView mTips;
	private AsyncM1Card reader;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m1card_activity);
		initView();
		initData();
	}

	private void initView() {
		mSpinnerCardType = (Spinner) findViewById(R.id.spinner_card_type);
		mAdapterCardType = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, cardtype);
		mAdapterCardType
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// 设置下拉列表的风格
		mSpinnerCardType.setAdapter(mAdapterCardType);

		mSpinnerPwdType = (Spinner) findViewById(R.id.spinner_pwd_type);
		mAdapterPwdType = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, pwdtype);
		mAdapterPwdType
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// 设置下拉列表的风格
		mSpinnerPwdType.setAdapter(mAdapterPwdType);
		mSpinnerPwdType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mKeyType = keyType[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		mEdShowCard = (EditText) findViewById(R.id.ed_card_num);
		mEdPwdA = (EditText) findViewById(R.id.ed_pwd_a);
		mEdPwdB = (EditText) findViewById(R.id.ed_pwd_b);
		mBlockNum = (EditText) findViewById(R.id.ed_block_num);
		mEdWriteData = (EditText) findViewById(R.id.ed_write_block);
		mReadData = (EditText) findViewById(R.id.ed_read_block);
		mEdWritePwd = (EditText) findViewById(R.id.ed_write_pwd);

		mBtnGetCardNum = (Button) findViewById(R.id.btn_getCardNum);
		mBtnSendPwd = (Button) findViewById(R.id.btn_sendCardPwd);
		mBtnValidPwd = (Button) findViewById(R.id.btn_validatePwd);
		mBtnWriteData = (Button) findViewById(R.id.btn_write);
		mBtnReadData = (Button) findViewById(R.id.btn_read);
		mBtnUpdate = (Button) findViewById(R.id.btn_update);
		mBtnGetCardNum.setOnClickListener(this);
		mBtnSendPwd.setOnClickListener(this);
		mBtnValidPwd.setOnClickListener(this);
		mBtnWriteData.setOnClickListener(this);
		mBtnReadData.setOnClickListener(this);
		mBtnUpdate.setOnClickListener(this);
		mTips = (TextView) findViewById(R.id.tips);
	}

	private void initData() {
		mEdPwdA.setText(DefaultKeyA);
		mEdPwdB.setText(DefaultKeyB);

		application = (MyApplication) this.getApplicationContext();

		reader = new AsyncM1Card(application.getHandlerThread().getLooper());
		reader.setOnReadCardNumListener(new OnReadCardNumListener() {

			@Override
			public void onReadCardNumSuccess(String num) {
				Log.d("jokey", "num:"+num);
				mEdShowCard.setText(num);
				mTips.setText(R.string.m1_str_get_success);
				cancleProgressDialog();
			}

			@Override
			public void onReadCardNumFail(int confirmationCode) {
				mEdShowCard.setText("");
				cancleProgressDialog();
				if (confirmationCode == M1CardAPI.Result.FIND_FAIL) {
					mTips.setText(R.string.no_card_with_data);
				} else if (confirmationCode == M1CardAPI.Result.TIME_OUT) {
					mTips.setText(R.string.no_card_without_data);
				} else if (confirmationCode == M1CardAPI.Result.OTHER_EXCEPTION) {
					mTips.setText(R.string.find_card_exception);
				}
			}
		});

		reader.setOnWriteAtPositionListener(new OnWriteAtPositionListener() {

			@Override
			public void onWriteAtPositionSuccess(String num) {
				cancleProgressDialog();
				mEdShowCard.setText(num);
				mTips.setText(R.string.writing_success);
			}

			@Override
			public void onWriteAtPositionFail(int comfirmationCode) {
				cancleProgressDialog();
				mTips.setText(R.string.writing_fail);
			}
		});
		reader.setOnReadAtPositionListener(new OnReadAtPositionListener() {

			@Override
			public void onReadAtPositionSuccess(String cardNum, byte[][] data) {
				cancleProgressDialog();
				mEdShowCard.setText(cardNum);
				if (data != null && data.length != 0) {
					mReadData.setText(DataUtils.toHexString(data[0]));
				}
				mTips.setText(R.string.reading_success);
			}

			@Override
			public void onReadAtPositionFail(int comfirmationCode) {
				cancleProgressDialog();
				mTips.setText(R.string.reading_fail);
			}
		});
		reader.setOnUpdatePwdListener(new OnUpdatePwdListener() {
			@Override
			public void onUpdatePwdSuccess(String num) {
				cancleProgressDialog();
				mEdShowCard.setText(num);
				mTips.setText(R.string.m1_str_update_pwd_success);
			}

			@Override
			public void onUpdatePwdFail(int comfirmationCode) {
				cancleProgressDialog();
				mTips.setText(R.string.m1_str_update_pwd_failure);
			}
		});
	}

	@Override
	public void onClick(View v) {
		boolean isExit = false;
		if (!SerialPortManager.getInstance().isOpen()
				&& !SerialPortManager.getInstance().openSerialPort()) {
			ToastUtil.showToast(this, R.string.open_serial_fail);
			isExit = true;
		}
		if (isExit) {
			return;
		}
		int block;
		String keyA = "";
		String keyB = "";
		String data = "";
		switch (v.getId()) {
		case R.id.btn_getCardNum:
			mEdShowCard.setText("");
			showProgressDialog(R.string.getcard_wait);
			reader.readCardNum();
			break;
		case R.id.btn_sendCardPwd:

			break;
		case R.id.btn_validatePwd:
			break;
		case R.id.btn_write:
			mEdShowCard.setText("");
			mReadData.setText("");
			if (TextUtils.isEmpty(mBlockNum.getText().toString())) {
				ToastUtil.showToast(this, R.string.m1_str_block_not_empty);
				return;
			}
			block = Integer.parseInt(mBlockNum.getText().toString());
			keyA = mEdPwdA.getText().toString();
			keyB = mEdPwdB.getText().toString();
			data = mEdWriteData.getText().toString();
			if (TextUtils.isEmpty(keyA) || TextUtils.isEmpty(keyB)
					|| TextUtils.isEmpty(data)) {
				ToastUtil.showToast(this, R.string.m1_str_all_not_empty);
				return;
			}
			if (RegexUtils.isCheckPwd(keyA) && RegexUtils.isCheckPwd(keyB)
					&& RegexUtils.isCheckWriteData(data)) {
				showProgressDialog(R.string.writing_wait);
				reader.write(block, mKeyType, NUM, keyA, keyB, data);
			} else
				ToastUtil.showToast(this, R.string.m1_str_all_not_validate);
			break;
		case R.id.btn_read:
			mReadData.setText("");
			mEdShowCard.setText("");
			if (TextUtils.isEmpty(mBlockNum.getText().toString())) {
				ToastUtil.showToast(this, R.string.m1_str_block_not_empty);
				return;
			}
			block = Integer.parseInt(mBlockNum.getText().toString());
			keyA = mEdPwdA.getText().toString();
			keyB = mEdPwdB.getText().toString();
			if (TextUtils.isEmpty(keyA) || TextUtils.isEmpty(keyB)) {
				ToastUtil.showToast(this, R.string.m1_str_not_empty);
				return;
			}
			showProgressDialog(R.string.reading_wait);
			reader.read(block, mKeyType, NUM, keyA, keyB);
			break;
		case R.id.btn_update:
			mReadData.setText("");
			mEdShowCard.setText("");
			if (TextUtils.isEmpty(mBlockNum.getText().toString()))// 另外块号需要校验
			{
				ToastUtil.showToast(this, R.string.m1_str_block_not_empty);
				return;
			}
			block = Integer.parseInt(mBlockNum.getText().toString());
			keyA = mEdPwdA.getText().toString();
			keyB = mEdPwdB.getText().toString();
			data = mEdWritePwd.getText().toString();
			if (TextUtils.isEmpty(keyA) || TextUtils.isEmpty(keyB)
					|| TextUtils.isEmpty(data)) {
				ToastUtil.showToast(this, R.string.m1_str_all_not_empty);
				return;
			}
			if (RegexUtils.isCheckPwd(keyA) && RegexUtils.isCheckPwd(keyB)
					&& RegexUtils.isCheckPwd(data)) {
				showProgressDialog(R.string.updatepwding_wait);
				reader.updatePwd(block, mKeyType, NUM, keyA, keyB, data);
			} else
				ToastUtil.showToast(this, R.string.m1_str_all_not_validate);
			break;
		default:
			break;
		}

	}

	private void showProgressDialog(int resId) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(resId));
		progressDialog.show();
	}

	private void cancleProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
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