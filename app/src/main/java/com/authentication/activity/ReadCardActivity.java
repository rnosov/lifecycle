package com.authentication.activity;

import com.authentication.asynctask.AsyncM1Card;
import com.authentication.asynctask.AsyncParseSFZ;
import com.authentication.asynctask.AsyncM1Card.OnReadAtPositionListener;
import com.authentication.asynctask.AsyncM1Card.OnReadCardNumListener;
import com.authentication.asynctask.AsyncParseSFZ.OnReadSFZListener;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android_serialport_api.M1CardAPI;
import android_serialport_api.ParseSFZAPI;
import android_serialport_api.ParseSFZAPI.People;

@SuppressLint("NewApi")
public class ReadCardActivity extends BaseActivity implements OnClickListener {
	private TextView sfz_name;
	private TextView sfz_sex;
	private TextView sfz_nation;
	private TextView sfz_year;
	private TextView sfz_mouth;
	private TextView sfz_day;
	private TextView sfz_address;
	private TextView sfz_id;
	private ImageView sfz_photo;

	private Button read_button;
	private Button stop_button;
	private Button back_button;

	private TextView resultInfo;
	private TextView rfidNum;

	private ProgressDialog progressDialog;

	private MyApplication application;

	private AsyncParseSFZ asyncParseSFZ;

	private AsyncM1Card reader;

	private int timeoutTime = 0;
	private int notFindTime = 0;
	private int validateFail = 0;
	private int readFail = 0;

	private static final int READ_SFZ = 1;
	private static final int READ_CARD_NUM = 2;

	private boolean isStop;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case READ_SFZ:
				clear();
				readSFZTime++;
				asyncParseSFZ.readSFZ(ParseSFZAPI.SECOND_GENERATION_CARD);
				break;
			case READ_CARD_NUM:
				readRFIDTime++;
				reader.readCardNum();
				break;
			default:
				break;
			}
		}
	};

	private int readSFZTime = 0;
	private int readSFZFailTime = 0;
	private int readSFZSuccessTime = 0;
	private int readSFZNotFind = 0;
	private int readSFZTimeout = 0;

	private int readRFIDTime = 0;
	private int readRFIDFailTime = 0;
	private int readRFIDSuccessTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_card_activity);
		initView();
		initData();
	}

	private void initView() {
		sfz_name = ((TextView) findViewById(R.id.sfz_name));
		sfz_nation = ((TextView) findViewById(R.id.sfz_nation));
		sfz_sex = ((TextView) findViewById(R.id.sfz_sex));
		sfz_year = ((TextView) findViewById(R.id.sfz_year));
		sfz_mouth = ((TextView) findViewById(R.id.sfz_mouth));
		sfz_day = ((TextView) findViewById(R.id.sfz_day));
		sfz_address = ((TextView) findViewById(R.id.sfz_address));
		sfz_id = ((TextView) findViewById(R.id.sfz_id));
		sfz_photo = ((ImageView) findViewById(R.id.sfz_photo));

		read_button = ((Button) findViewById(R.id.readCardButton));
		stop_button = ((Button) findViewById(R.id.stopReadCardButton));
		back_button = ((Button) findViewById(R.id.backButton));
		rfidNum = ((TextView) findViewById(R.id.rfidNum));
		resultInfo = ((TextView) findViewById(R.id.resultInfo2));

		read_button.setOnClickListener(this);
		stop_button.setOnClickListener(this);
		back_button.setOnClickListener(this);
	}

	private void initData() {
		application = (MyApplication) this.getApplicationContext();

		asyncParseSFZ = new AsyncParseSFZ(application.getHandlerThread()
				.getLooper(), application.getRootPath(), this);
		reader = new AsyncM1Card(application.getHandlerThread().getLooper());
		asyncParseSFZ.setOnReadSFZListener(new OnReadSFZListener() {
			@Override
			public void onReadSuccess(People people) {
				cancleProgressDialog();
				updateInfo(people);
				readSFZSuccessTime++;
				refresh();
				if (!isStop) {
					mHandler.sendEmptyMessageDelayed(READ_CARD_NUM, 3);
				}
			}

			@Override
			public void onReadFail(int result) {
				cancleProgressDialog();
				if (result == ParseSFZAPI.Result.FIND_FAIL) {
					readSFZNotFind++;
				} else if (result == ParseSFZAPI.Result.TIME_OUT) {
					readSFZTimeout++;
				} else if (result == ParseSFZAPI.Result.OTHER_EXCEPTION) {
				}
				readSFZFailTime++;
				refresh();
				if (!isStop) {
					mHandler.sendEmptyMessageDelayed(READ_CARD_NUM, 3);
				}
			}
		});

		reader.setOnReadCardNumListener(new OnReadCardNumListener() {

			@Override
			public void onReadCardNumSuccess(String num) {
				cancleProgressDialog();
				rfidNum.setText(num);
				readRFIDSuccessTime++;
				refresh();
				if (!isStop) {
					mHandler.sendEmptyMessageDelayed(READ_CARD_NUM, 5);
				}
			}

			@Override
			public void onReadCardNumFail(int confirmationCode) {
				cancleProgressDialog();
				rfidNum.setText("");
				if (confirmationCode == M1CardAPI.Result.FIND_FAIL) {
				} else if (confirmationCode == M1CardAPI.Result.TIME_OUT) {
				} else if (confirmationCode == M1CardAPI.Result.OTHER_EXCEPTION) {
				}
				readRFIDFailTime++;
				refresh();
				if (!isStop) {
					mHandler.sendEmptyMessageDelayed(READ_CARD_NUM, 5);
				}
			}
		});

		reader.setOnReadAtPositionListener(new OnReadAtPositionListener() {

			@Override
			public void onReadAtPositionSuccess(String num, byte[][] data) {
				cancleProgressDialog();
				rfidNum.setText("cardNum:" + num + "\ndata：" + new String(data[0]));
				readRFIDSuccessTime++;
				refresh();
				if (!isStop) {
					mHandler.sendEmptyMessageDelayed(READ_SFZ, 3);
				}
			}

			@Override
			public void onReadAtPositionFail(int comfirmationCode) {
				cancleProgressDialog();
				rfidNum.setText("");
				if (comfirmationCode == M1CardAPI.Result.FIND_FAIL) {
					notFindTime++;
				} else if (comfirmationCode == M1CardAPI.Result.TIME_OUT) {
					timeoutTime++;
				} else if (comfirmationCode == M1CardAPI.Result.VALIDATE_FAIL) {
					validateFail++;
				} else if (comfirmationCode == M1CardAPI.Result.READ_FAIL) {
					readFail++;
				}
				readRFIDFailTime++;
				refresh();
				if (!isStop) {
					mHandler.sendEmptyMessageDelayed(READ_SFZ, 3);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.readCardButton:
			clear();
			isStop = false;
			mHandler.sendEmptyMessage(READ_CARD_NUM);
			Log.i("whw", "read_sfz");
			break;
		case R.id.stopReadCardButton:
			stopRead();
			break;
		case R.id.backButton:
			finish();
			break;
		default:
			break;
		}
	}

	private void refresh() {
		String result = "totals：" + readRFIDTime + " success："
				+ readRFIDSuccessTime + "  fails：" + readRFIDFailTime + "\n"
				+ "outTime：" + timeoutTime + "no found card:" + notFindTime
				+ "validateFail:" + validateFail + "read card fail：" + readFail;
		Log.i("whw", "result=" + result);
		resultInfo.setText(result);
	}

	private void stopRead() {
		cancleProgressDialog();
		isStop = true;

		mHandler.removeMessages(READ_CARD_NUM);
		mHandler.removeMessages(READ_SFZ);
	}

	@Override
	protected void onPause() {
		stopRead();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		stopRead();
		super.onStop();
	}

	private void updateInfo(People people) {
		sfz_address.setText(people.getPeopleAddress());
		sfz_day.setText(people.getPeopleBirthday().substring(6));
		sfz_id.setText(people.getPeopleIDCode());
		sfz_mouth.setText(people.getPeopleBirthday().substring(4, 6));
		sfz_name.setText(people.getPeopleName());
		sfz_nation.setText(people.getPeopleNation());
		sfz_sex.setText(people.getPeopleSex());
		sfz_year.setText(people.getPeopleBirthday().substring(0, 4));
		Bitmap photo = BitmapFactory.decodeByteArray(people.getPhoto(), 0,
				people.getPhoto().length);
		sfz_photo.setBackgroundDrawable(new BitmapDrawable(photo));
	}

	private void clear() {
		rfidNum.setText("");
	}

	private void cancleProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}
}