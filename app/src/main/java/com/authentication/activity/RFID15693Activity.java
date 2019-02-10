package com.authentication.activity;

import com.authentication.asynctask.AsyncRFID15693Card;
import com.authentication.asynctask.AsyncRFID15693Card.OnFindCardListener;
import com.authentication.asynctask.AsyncRFID15693Card.OnInitListener;
import com.authentication.asynctask.AsyncRFID15693Card.OnReadListener;
import com.authentication.asynctask.AsyncRFID15693Card.OnReadMoreListener;
import com.authentication.asynctask.AsyncRFID15693Card.OnWriteListener;
import com.authentication.asynctask.AsyncRFID15693Card.OnWriteMoreListener;
import com.authentication.utils.DataUtils;
import com.authentication.utils.ToastUtil;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RFID15693Activity extends BaseActivity implements OnClickListener {
	private Button init;
	private Button find;
	private TextView dsfid;
	private TextView uid;
	private Button read;
	private EditText readPosition;
	private TextView readInfo;
	private Button write;
	private EditText writePosition;
	private EditText writeText;
	
	private Button readMore;
	private EditText readMorePosition;
	private TextView readMoreInfo;
	
	private Button writeMore;
	private EditText writeMorePosition;
	private EditText writeMoreText;
	
	private AsyncRFID15693Card reader;
	
	private MyApplication application;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rfid_15693_activity);
		initView();
		initData();
	}

	private void initView() {
		init = (Button) findViewById(R.id.init);
		find = (Button) findViewById(R.id.find);
		dsfid = (TextView) findViewById(R.id.dsfid);
		uid = (TextView) findViewById(R.id.uid);
		read = (Button) findViewById(R.id.read_15693);
		readPosition = (EditText) findViewById(R.id.read_position_15693);
		readInfo = (TextView) findViewById(R.id.read_info_15693);
		write = (Button) findViewById(R.id.write_15693);
		writePosition = (EditText) findViewById(R.id.write_position_15693);
		writeText = (EditText) findViewById(R.id.write_text_15693);
		
		
		readMore = (Button) findViewById(R.id.read_more_15693);
		readMorePosition = (EditText) findViewById(R.id.read_more_position_15693);
		readMoreInfo = (TextView) findViewById(R.id.read_more_info_15693);
		
		writeMore = (Button) findViewById(R.id.write_more_15693);
		writeMorePosition = (EditText) findViewById(R.id.write_more_position_15693);
		writeMoreText = (EditText) findViewById(R.id.write_more_text_15693);
		
		init.setOnClickListener(this);
		find.setOnClickListener(this);
		read.setOnClickListener(this);
		write.setOnClickListener(this);
		writeMore.setOnClickListener(this);
		readMore.setOnClickListener(this);
	}

	private void initData() {
		application = (MyApplication) getApplication();
		reader = new AsyncRFID15693Card(application.getHandlerThread().getLooper());
		
		reader.setOnInitListener(new OnInitListener() {
			
			@Override
			public void initSuccess() {
				ToastUtil.showToast(RFID15693Activity.this, R.string.init_success);
			}
			
			@Override
			public void initFail() {
				ToastUtil.showToast(RFID15693Activity.this, R.string.init_fail);
			}
		});
		
		reader.setOnFindCardListener(new OnFindCardListener() {
			
			@Override
			public void findSuccess(byte[] data) {
				dsfid.setText(DataUtils.byte2Hexstr(data[0]));
				byte[] uidData = new byte[8];
				System.arraycopy(data, 1, uidData, 0, uidData.length);
				uid.setText(DataUtils.toHexString(uidData));
			}
			
			@Override
			public void findFail() {
				ToastUtil.showToast(RFID15693Activity.this, R.string.find_fail);
			}
		});
		
		reader.setOnReadListener(new OnReadListener() {
			
			@Override
			public void readSuccess(byte[] data) {
				readInfo.setText(new String(data));
			}
			
			@Override
			public void readFail() {
				ToastUtil.showToast(RFID15693Activity.this, R.string.read_15693_fail);
			}
		});
		
		reader.setOnReadMoreListener(new OnReadMoreListener() {
			
			@Override
			public void readMoreSuccess(byte[] data) {
				readMoreInfo.setText(new String(data));
			}
			
			@Override
			public void readMoreFail() {
				ToastUtil.showToast(RFID15693Activity.this, R.string.read_15693_fail);
			}
		});
		
		reader.setOnWriteListener(new OnWriteListener() {
			
			@Override
			public void writeSuccess() {
				ToastUtil.showToast(RFID15693Activity.this, R.string.write_15693_success);
			}
			
			@Override
			public void writeFail() {
				ToastUtil.showToast(RFID15693Activity.this, R.string.write_15693_fail);
			}
		});
		
		reader.setOnWriteMoreListener(new OnWriteMoreListener() {
			
			@Override
			public void writeMoreSuccess() {
				ToastUtil.showToast(RFID15693Activity.this, R.string.write_15693_success);
			}
			
			@Override
			public void writeMoreFail() {
				ToastUtil.showToast(RFID15693Activity.this, R.string.write_15693_fail);
			}
		});
	}
	
	int positionCount = 0;

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
		case R.id.init:
			reader.init();
			break;
		case R.id.find:
			reader.findCard();
			break;
		case R.id.read_15693:
			String str1 = readPosition.getEditableText().toString();
			if(TextUtils.isEmpty(str1)){
				ToastUtil.showToast(this, R.string.no_null_toast);
				return;
			}
			int position1 = Integer.parseInt(str1);
			reader.read(position1);
			break;
		case R.id.write_15693:
			String str2 = writePosition.getEditableText().toString();
			String str3 = writeText.getEditableText().toString();
			if(TextUtils.isEmpty(str2)||TextUtils.isEmpty(str3)){
				ToastUtil.showToast(this, R.string.no_null_toast);
				return;
			}
			int position2 = Integer.parseInt(str2);
			byte[] data = new byte[4];
			byte[] temp = str3.getBytes();
			if(temp.length>=4){
				System.arraycopy(temp, 0, data, 0, 4);
			}else if(temp.length<4){
				System.arraycopy(temp, 0, data, 0, temp.length);
			}
			reader.write(position2, data);
			break;
			
		case R.id.write_more_15693:
			String str4 = writeMorePosition.getEditableText().toString();
			String str5 = writeMoreText.getEditableText().toString();
			if(TextUtils.isEmpty(str4)||TextUtils.isEmpty(str5)){
				ToastUtil.showToast(this, R.string.no_null_toast);
				return;
			}
			int position3 = Integer.parseInt(str4);
			byte[] moreData = str5.getBytes();
			positionCount = moreData.length % 4 == 0 ? moreData.length / 4
					: moreData.length / 4 + 1;
			reader.writeMore(position3, moreData);
			Log.i("whw", "positionCount="+positionCount+"     moreData.length="+moreData.length);
			break;
			
		case R.id.read_more_15693:
			String str10 = readMorePosition.getEditableText().toString();
			if(TextUtils.isEmpty(str10)){
				ToastUtil.showToast(this, R.string.no_null_toast);
				return;
			}
			int position10 = Integer.parseInt(str10);
			reader.readMore(position10,positionCount);
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
