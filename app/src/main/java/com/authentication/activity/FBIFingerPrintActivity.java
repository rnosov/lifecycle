package com.authentication.activity;

import java.util.List;

import com.authentication.utils.DataUtils;
import com.authentication.utils.FBIFingerModel;
import com.authentication.utils.FingerDBOperation;
import com.authentication.utils.ToastUtil;
import com.upek.android.ptapi.PtConstants;
import com.upek.android.ptapi.struct.PtInputBir;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.FBIFingerPrintAPI;
import android_serialport_api.FBI.FingerId;

public class FBIFingerPrintActivity extends Activity implements OnClickListener {
	public static final String mDSN[] = { "usb,timeout=500", "wbf,timeout=500" };
	// Windows DSN for serial connection
	public static final String msDSNSerial = "sio,port=COM1,speed=115200,timeout=2000";
	// Linux DSN for serial connection
	// public static final String msDSNSerial =
	// "sio,port=/dev/ttyS0,speed=115200,timeout=2000";
	public static final int miUseSerialConnection = 0;
	public static final int INIT_COMPLETED = 1001;
	public static final int INIT_FAIL = 1002;
	public static final int EXIT = 1003;
	private FBIFingerPrintAPI api;
	private FingerDBOperation mOperation;
	private ProgressDialog progressDialog;
	//Button
	private Button btnVerifyAll, btnNavigateRaw, btnNavigateMouse, btnNavigateDiscrete, btnDeleteAll, btnGrab, btnQuit,btnVerify;
	
	//save
	private Button btnRegist,btnSave,btnCompare,btnClear;
	private EditText etID;
	private PtInputBir template;
	private byte[] templateISO;
	//finger image
	private ImageView fingerImage;
	/**
	 * Transfer messages to the main activity thread.
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(Message aMsg) {
			switch (aMsg.what) {
			case FBIFingerPrintAPI.SHOW_MESSAGE:
				((TextView) findViewById(R.id.EnrollmentTextView)).setText((String) aMsg.obj);
				break;
			case FBIFingerPrintAPI.GET_FINGER_IMAGE:
				Bitmap image = (Bitmap)aMsg.obj;
				if(image!=null)
					fingerImage.setImageBitmap((Bitmap)aMsg.obj);
				break;
			case FBIFingerPrintAPI.GET_FINGER_TEMPLATE:
				Log.d("jokey", "GET_FINGER_TEMPLATE");
				template = (PtInputBir)aMsg.obj;
				Log.d("jokey", "template: "+DataUtils.toHexString(template.bir.data));
				break;
			case FBIFingerPrintAPI.GET_FINGER_TEMPLATE_ISO:
				Log.d("jokey", "GET_FINGER_TEMPLATE");
				templateISO = (byte[])aMsg.obj;
				Log.d("jokey", "template: "+DataUtils.toHexString(templateISO));
				break;
			case INIT_COMPLETED:
				initView();
				cancleProgressDialog();
				break;
			case INIT_FAIL:
				cancleProgressDialog();
				ToastUtil.showToast(FBIFingerPrintActivity.this, "Init fail.");
				break;
			case EXIT:
				cancleProgressDialog();
				ToastUtil.showToast(getApplicationContext(), (String)aMsg.obj);
				break;
			default:
				break;
			}
		}
	};
	/** Initialize activity and obtain PTAPI session. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fbi);

		init();

	}
	
	private void init() {
		showProgressDialog("Initializing...");
		api = new FBIFingerPrintAPI(getApplicationContext(), mHandler);
		mOperation = new FingerDBOperation(this);
		mOperation = new FingerDBOperation(this);
		new Thread(){
			public void run() {
				// Power on
				// api.powerOn();
				// Load PTAPI library and initialize its interface
				if (api.initializePtapi()) {
					// Open PTAPI session
					api.openPtapiSession();
					// If PTAPI session is available, register listeners for buttons
					if (api.getmConn() != null) {
						mHandler.sendEmptyMessage(INIT_COMPLETED);
					}else{
						mHandler.sendEmptyMessage(INIT_FAIL);
					}
				}else{
					mHandler.sendEmptyMessage(INIT_FAIL);
				}
			};
		}.start();
		
	}
	private void initView(){

		setEnrollButtonListener(R.id.ButtonLeftThumb, FingerId.LEFT_THUMB);
		setEnrollButtonListener(R.id.ButtonLeftIndex, FingerId.LEFT_INDEX);
		setEnrollButtonListener(R.id.ButtonLeftMiddle, FingerId.LEFT_MIDDLE);
		setEnrollButtonListener(R.id.ButtonLeftRing, FingerId.LEFT_RING);
		setEnrollButtonListener(R.id.ButtonLeftLittle, FingerId.LEFT_LITTLE);

		setEnrollButtonListener(R.id.ButtonRightThumb, FingerId.RIGHT_THUMB);
		setEnrollButtonListener(R.id.ButtonRightIndex, FingerId.RIGHT_INDEX);
		setEnrollButtonListener(R.id.ButtonRightMiddle, FingerId.RIGHT_MIDDLE);
		setEnrollButtonListener(R.id.ButtonRightRing, FingerId.RIGHT_RING);
		setEnrollButtonListener(R.id.ButtonRightLittle, FingerId.RIGHT_LITTLE);
		
		//--------------------------------------------------------------------------------
		btnVerifyAll = (Button) findViewById(R.id.ButtonVerifyAll);
		btnVerify = (Button) findViewById(R.id.ButtonVerify);
		btnNavigateRaw = (Button) findViewById(R.id.ButtonNavigateRaw);
		btnNavigateMouse = (Button) findViewById(R.id.ButtonNavigateMouse);
		btnNavigateDiscrete = (Button) findViewById(R.id.ButtonNavigateDiscrete);
		btnDeleteAll = (Button) findViewById(R.id.ButtonDeleteAll);
		btnGrab = (Button) findViewById(R.id.ButtonGrab);
		btnQuit = (Button) findViewById(R.id.ButtonQuit);
		fingerImage = (ImageView) findViewById(R.id.fbi_image);
		
		btnVerifyAll.setOnClickListener(this);
		btnVerify.setOnClickListener(this);
		btnNavigateRaw.setOnClickListener(this);
		btnNavigateMouse.setOnClickListener(this);
		btnNavigateDiscrete.setOnClickListener(this);
		btnDeleteAll.setOnClickListener(this);
		btnGrab.setOnClickListener(this);
		btnQuit.setOnClickListener(this);
		
		//--------------------------------------------------------------------------------
		btnRegist = (Button) findViewById(R.id.fbi_regist);
		btnSave = (Button) findViewById(R.id.fbi_save);
		btnCompare = (Button) findViewById(R.id.fbi_verify);
		btnClear = (Button) findViewById(R.id.fbi_clear);
		etID = (EditText) findViewById(R.id.fbi_etID);
		
		btnRegist.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		btnCompare.setOnClickListener(this);
		btnClear.setOnClickListener(this);
		// disable navigation for area sensors
		if ((api.getmSensorInfo().sensorType & PtConstants.PT_SENSORBIT_STRIP_SENSOR) == 0) {
			btnNavigateDiscrete.setVisibility(Button.GONE);
			btnNavigateMouse.setVisibility(Button.GONE);
			btnNavigateRaw.setVisibility(Button.GONE);
		}
	
	}
	private long lastBackTime = 0;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			long currentBackTime = System.currentTimeMillis();
			if (2000 < currentBackTime - lastBackTime) {
				ToastUtil.showToast(this, "Please press back again to exit!");
				lastBackTime = currentBackTime;
			} else {
				showProgressDialog("Exiting...");
				new Thread(){
					public void run() {
						if(api.destroyAll()){
							mHandler.obtainMessage(EXIT,"Exit success!").sendToTarget();
						}else{
							mHandler.obtainMessage(EXIT,"Exit failed!").sendToTarget();
						}
						android.os.Process.killProcess(android.os.Process.myPid());
					};
				}.start();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onDestroy() {
		api.destroyAll();
		super.onDestroy();
	}
	/**
	 * Set listener for an enrollment button.
	 * 
	 * @param buttonId
	 *            Resource ID of a button
	 * @param fingerId
	 *            Finger ID.
	 */
	private void setEnrollButtonListener(final int buttonId, final int fingerId) {
		Button aButton = (Button) findViewById(buttonId);
		OnClickListener aListener = new View.OnClickListener() {
			public void onClick(View view) {
				api.enroll(fingerId);
			}
		};
		aButton.setOnClickListener(aListener);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ButtonVerifyAll:
			api.VerifyAll();//Verify finger
			break;
		case R.id.ButtonVerify:
			api.Verify(template);//Verify finger
			break;
		case R.id.ButtonNavigateRaw:
			api.navigationRaw();
			break;
		case R.id.ButtonNavigateMouse:
			api.navigationMouse();
			break;
		case R.id.ButtonNavigateDiscrete:
			api.navigationDiscrete();
			break;
		case R.id.ButtonDeleteAll:
			api.deleteAll();//Delete finger
			break;
		case R.id.ButtonGrab:
			api.grabImage(FBIFingerPrintActivity.this);//Grab finger image
			break;
		case R.id.ButtonQuit:
			api.destroyAll();
			finish();
			break;
		//-----------------------------------------------------------------------------------------
		case R.id.fbi_regist:
//			api.enroll(FingerId.LEFT_THUMB);//This finger id can be fill by yourself here.
			api.capture();
			break;
		case R.id.fbi_save:
			save();
			break;
		case R.id.fbi_verify:
			verify();
			break;
		case R.id.fbi_clear:
			if(mOperation.deleteAllTemplate())
				ToastUtil.showToast(getApplicationContext(), "Delete success!");
			else
				ToastUtil.showToast(getApplicationContext(), "Delete fail!");
			break;
		}
	}
	/**
	 * Get finger template from outside database,then verify with a live finger.
	 */
	private void verify() {
		String id = etID.getText().toString();
		if(TextUtils.isEmpty(id)){
			ToastUtil.showToast(getApplicationContext(), "Can not empty!");
		}else{
			List<FBIFingerModel> fingers = mOperation.getTemplates();
			if(fingers.size()>0){
				for (int i = 0; i < fingers.size(); i++) {
					FBIFingerModel finger = fingers.get(i);
					if(id.equals(finger.getModel_ID())){
						api.Verify(finger.getTemplate());
						break;
					}
				}
			}
			else
				ToastUtil.showToast(getApplicationContext(), "No fingers!");
		}
	}
	/**
	 * Save finger data to outside database
	 */
	private void save() {
		String id = etID.getText().toString();
		if(TextUtils.isEmpty(id)){
			ToastUtil.showToast(getApplicationContext(), "Can not empty!");
		}else{
			List<FBIFingerModel> fingers = mOperation.getTemplates();
			if(fingers.size()>0){
				for (int i = 0; i < fingers.size(); i++) {
					FBIFingerModel finger = fingers.get(i);
					if(id.equals(finger.getModel_ID())){
						ToastUtil.showToast(getApplicationContext(), "This ID has been saved");
						return;
					}
				}
			}
			if(template!=null){
				FBIFingerModel finger = new FBIFingerModel();
				finger.setModel_ID(id);
				finger.setTemplate(template);
				if(mOperation.saveTemplate(finger)){
					Toast.makeText(this, "Save success!", 0).show();
					template = null;
				}else{
					Toast.makeText(this, "Save fail!", 0).show();
				}
			}else{
				Toast.makeText(this, "Please regist finger firstly!", 0).show();
			}
			
		}
		
	}
	private void cancleProgressDialog() {
		if (null != progressDialog && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	private void showProgressDialog(String message) {
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setMessage(message);
		this.progressDialog.setCanceledOnTouchOutside(false);
		if (!this.progressDialog.isShowing()) {
			this.progressDialog.show();
		}
	}
}