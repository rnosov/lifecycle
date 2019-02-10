package com.authentication.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.litepal.crud.DataSupport;

import com.authentication.asynctask.AsyncFingerprint;
import com.authentication.asynctask.AsyncFingerprint.OnEmptyListener;
import com.authentication.asynctask.AsyncFingerprint.OnCalibrationListener;
import com.authentication.utils.ToastUtil;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import android_serialport_api.FingerprintAPI;

//import com.machinezoo.sourceafis.FingerprintTemplate;

public class FingerprintActivity extends BaseActivity implements OnClickListener {
	private String[] m;

	private AsyncFingerprint asyncFingerprint;

	private Spinner spinner;

	private ArrayAdapter<String> adapter;

	private Button register, validate, register2, validate2, clear, calibration, back, register3, save, compares,
			dbclear;

	private EditText ID;

	private ImageView fingerprintImage;

	private ProgressDialog progressDialog;

	private byte[] model;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case AsyncFingerprint.SHOW_PROGRESSDIALOG:
				cancleProgressDialog();
				showProgressDialog((Integer) msg.obj);
				break;
			case AsyncFingerprint.SHOW_FINGER_IMAGE:
				// imageNum++;
				// upfail.setText("上传成功：" + imageNum + "\n" + EAR"上传失败：" +
				// failTime+ "\n" + "解析出错：" + missPacket);
				//signaturesend(msg.arg1, (byte[]) msg.obj);
				showFingerImage(msg.arg1, (byte[]) msg.obj);
				break;
			case AsyncFingerprint.SHOW_FINGER_MODEL:
				FingerprintActivity.this.model = (byte[]) msg.obj;
				if (FingerprintActivity.this.model != null) {
					Log.i("whw", "#################model.length=" + FingerprintActivity.this.model.length);
				}
				cancleProgressDialog();
				// ToastUtil.showToast(FingerprintActivity.this,
				// "pageId="+msg.arg1+" store="+msg.arg2);
				break;
			case AsyncFingerprint.REGISTER_SUCCESS:
				cancleProgressDialog();
				if (msg.obj != null) {
					Integer id = (Integer) msg.obj;
					ToastUtil.showToast(FingerprintActivity.this,
							getString(R.string.register_success) + "  pageId=" + id);
				} else {
					ToastUtil.showToast(FingerprintActivity.this, R.string.register_success);
				}

				break;
			case AsyncFingerprint.REGISTER_FAIL:
				cancleProgressDialog();
				ToastUtil.showToast(FingerprintActivity.this, R.string.register_fail);
				break;
			case AsyncFingerprint.VALIDATE_RESULT1:
				cancleProgressDialog();
				showValidateResult((Boolean) msg.obj);
				break;
			case AsyncFingerprint.VALIDATE_RESULT2:
				cancleProgressDialog();
				Integer r = (Integer) msg.obj;
				if (r != -1) {
					ToastUtil.showToast(FingerprintActivity.this,
							getString(R.string.verifying_through) + "  pageId=" + r);
				} else {
					showValidateResult(false);
				}
				break;
			case AsyncFingerprint.UP_IMAGE_RESULT:
				cancleProgressDialog();
				ToastUtil.showToast(FingerprintActivity.this, (Integer) msg.obj);
				// failTime++;
				// upfail.setText("上传成功：" + imageNum + "\n" + "上传失败：" +
				// failTime+ "\n" + "解析出错：" + missPacket);
				break;
			case AsyncFingerprint.VERIFYMY:
				cancleProgressDialog();
				ToastUtil.showToast(FingerprintActivity.this, (Integer) msg.obj+"");


			default:
				break;
			}
		}

	};

	private void signaturesend(int arg1, byte[] obj) {

		SendToServer send = new SendToServer();

		send.execute(obj);
	}

	class SendToServer extends AsyncTask<byte[],Void,Void>{
		@Override
		protected Void doInBackground(byte[]... bytes) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://fc6ae139.ngrok.io/fingerprint");
			//httpPost.setHeader(HTTP.CONTENT_TYPE,	"application/x-www-form-urlencoded;charset=UTF-8");
			// Add your data

			//httpPost.setEntity(new ByteArrayEntity(bytes[0]));
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			//MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			//entityBuilder.addPart("name", new StringBody("Name"));
			//entityBuilder.addPart("Id", new StringBody("ID"));
			//entityBuilder.addPart("title",new StringBody("TITLE"));
			//entityBuilder.addPart("caption", new StringBody("Caption"));
				//ByteArrayOutputStream bos = new ByteArrayOutputStream();
				//bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
				byte[] data = bytes[0];//bos.toByteArray();
				ByteArrayBody bab = new ByteArrayBody(data, "forest.jpg");
				entityBuilder.addPart("file", bab);
			//postRequest.setEntity(reqEntity);
			httpPost.setEntity(entityBuilder.build());
			HttpResponse response = null;
			try {
				response = httpClient.execute(httpPost);
			}
			catch(Exception e){
				Log.e("error",e.getMessage());
			}
			Log.d("test",response.toString());

			return null;
		}
	}

	private void showValidateResult(boolean matchResult) {
		if (matchResult) {
			ToastUtil.showToast(FingerprintActivity.this, R.string.verifying_through);
		} else {
			ToastUtil.showToast(FingerprintActivity.this, R.string.verifying_fail);
		}
	}

	private void showFingerImage(int fingerType, byte[] data) {
		Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
		// saveImage(data);
		fingerprintImage.setBackgroundDrawable(new BitmapDrawable(image));
		writeToFile(data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fingerprint);

		initView();
		initViewListener();
		initData();

	}

	private void initView() {
		spinner = (Spinner) findViewById(R.id.spinner);
		register = (Button) findViewById(R.id.register);
		validate = (Button) findViewById(R.id.validate);
		register2 = (Button) findViewById(R.id.register2);
		validate2 = (Button) findViewById(R.id.validate2);
		clear = (Button) findViewById(R.id.clear_flash);
		calibration = (Button) findViewById(R.id.calibration);
		back = (Button) findViewById(R.id.backRegister);
		register3 = (Button) findViewById(R.id.bt_registe);
		register3.setOnClickListener(this);
		save = (Button) findViewById(R.id.bt_save);
		save.setOnClickListener(this);
		compares = (Button) findViewById(R.id.bt_compares);
		compares.setOnClickListener(this);
		dbclear = (Button) findViewById(R.id.bt_clear);
		dbclear.setOnClickListener(this);
		ID = (EditText) findViewById(R.id.et_fingerId);
		fingerprintImage = (ImageView) findViewById(R.id.fingerprintImage);
		Button bt1 = (Button) findViewById(R.id.btn1_my);
		bt1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				asyncFingerprint.verifyMy();				
			}
		});

	}

	private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

	private void writeToFile(byte[] data) {
		String dir = rootPath + "/fingerprint_image";
		File dirPath = new File(dir);
		if (!dirPath.exists()) {
			dirPath.mkdir();
		}

		String filePath = dir + "/" + System.currentTimeMillis() + ".bmp";
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream fos = null;
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initData() {
		m = this.getResources().getStringArray(R.array.fingerprint_size);

		// 将可选内容与ArrayAdapter连接起来
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m);

		// 设置下拉列表的风格
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// 将adapter 添加到spinner中
		spinner.setAdapter(adapter);

		// 添加事件Spinner事件监听
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());

	}

	// 使用数组形式操作
	class SpinnerSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
			Log.i("whw", "position=" + position);
			switch (position) {
			case 0:
				asyncFingerprint.setFingerprintType(FingerprintAPI.SMALL_FINGERPRINT_SIZE);
				break;
			case 1:
				asyncFingerprint.setFingerprintType(FingerprintAPI.BIG_FINGERPRINT_SIZE);
				break;
			default:
				break;
			}
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	}

	private void initData2() {
		asyncFingerprint = new AsyncFingerprint(handlerThread.getLooper(), mHandler);

		asyncFingerprint.setOnEmptyListener(new OnEmptyListener() {

			@Override
			public void onEmptySuccess() {
				ToastUtil.showToast(FingerprintActivity.this, R.string.clear_flash_success);

			}

			@Override
			public void onEmptyFail() {
				ToastUtil.showToast(FingerprintActivity.this, R.string.clear_flash_fail);

			}
		});

		asyncFingerprint.setOnCalibrationListener(new OnCalibrationListener() {

			@Override
			public void onCalibrationSuccess() {
				Log.i("whw", "onCalibrationSuccess");
				ToastUtil.showToast(FingerprintActivity.this, R.string.calibration_success);
			}

			@Override
			public void onCalibrationFail() {
				Log.i("whw", "onCalibrationFail");
				ToastUtil.showToast(FingerprintActivity.this, R.string.calibration_fail);

			}
		});

	}

	private void initViewListener() {
		register.setOnClickListener(this);
		validate.setOnClickListener(this);
		register2.setOnClickListener(this);
		validate2.setOnClickListener(this);
		calibration.setOnClickListener(this);
		clear.setOnClickListener(this);
		back.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.register:
			asyncFingerprint.setFingerprintType(FingerprintAPI.BIG_FINGERPRINT_SIZE);
			asyncFingerprint.setStop(false);
			asyncFingerprint.register();
			//asyncFingerprint.PS_UpChar();
			break;
		case R.id.validate:
			if (model != null) {
				asyncFingerprint.validate(model);
			} else {
				ToastUtil.showToast(FingerprintActivity.this, R.string.first_register);
			}
			break;
		case R.id.register2:
			asyncFingerprint.register2();
			break;
		case R.id.validate2:
			asyncFingerprint.validate2();
			break;
		case R.id.calibration:
			Log.i("whw", "calibration start");
			asyncFingerprint.PS_Calibration();
			break;
		case R.id.clear_flash:
			asyncFingerprint.PS_Empty();
			break;
		case R.id.backRegister:
			finish();
			break;
		case R.id.bt_registe:
			asyncFingerprint.setStop(false);
			asyncFingerprint.register();
			break;
		case R.id.bt_save:
			FingerModels fingerModels = new FingerModels();
			String id = ID.getText().toString();
			if (!TextUtils.isEmpty(id)) {
				fingerModels.setId(id);
				fingerModels.setModel(model);
				fingerModels.save();
				if (fingerModels.save()) {
					ToastUtil.showToast(getApplicationContext(), "save success");
				} else {
					ToastUtil.showToast(getApplicationContext(), "save fail");
				}
			} else {
				ToastUtil.showToast(getApplicationContext(), R.string.noID);
			}
			break;
		case R.id.bt_compares:
			if (TextUtils.isEmpty(ID.getText())) {
				ToastUtil.showToast(getApplicationContext(), R.string.noID);
			} else {
				List<FingerModels> list = DataSupport.select("model").where("model_ID = ?", ID.getText().toString())
						.find(FingerModels.class);
				if (list.size() > 0) {
					asyncFingerprint.validate(list.get(0).getModel());
				} else {
					ToastUtil.showToast(getApplicationContext(), "no model");
				}
			}
			break;
		case R.id.bt_clear:
			DataSupport.deleteAll(FingerModels.class);
			ToastUtil.showToast(FingerprintActivity.this, R.string.clear_flash_success);
			break;
		default:
			break;
		}
	}

	private void showProgressDialog(int resId) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getResources().getString(resId));
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_BACK == keyCode) {
					asyncFingerprint.setStop(true);
				}
				return false;
			}
		});
		progressDialog.show();
	}

	private void cancleProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		cancleProgressDialog();
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i("whw", "onRestart");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i("whw", "onStop");
	}

	@Override
	protected void onResume() {
		super.onResume();
		initData2();
		Log.i("whw", "onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		cancleProgressDialog();
		asyncFingerprint.setStop(true);
		Log.i("whw", "onPause");
	}
}