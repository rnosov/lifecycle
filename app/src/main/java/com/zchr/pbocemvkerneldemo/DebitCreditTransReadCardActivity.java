package com.zchr.pbocemvkerneldemo;

import com.authentication.activity.R;
import com.example.iccardapi.IcInterface;
import com.zchr.jni.fromnative.PbocEmvInterface;
import com.zchr.jni.tonative.TermInterface;
import com.zchr.nfc.NfcReader;
import com.zchr.nfc.NfcReaderBaseActivity;
import com.zchr.util.DefineFinal;
import com.zchr.util.MyUtil;
import com.zchr.util.ReturnStringData;
import com.zchr.util.ScreenShowInfo;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DebitCreditTransReadCardActivity extends NfcReaderBaseActivity implements OnClickListener {

	//屏幕行数
	public static final int SCREEN_SHOW_LINE_MAX = 8;

	//屏幕列数
	public static final int SCREEN_SHOW_COL_MAX = 32;

	//屏幕显示内容
	private String[] m_astrScreenShow;

	//上下文
	public Activity m_activityMain;

	//显示TextView
	public TextView m_textViewScreenShow;

	//处理状态,0-准备、1-处理中
	private int m_iProcState;
	private static final byte[] CMD_OPEN_SWITCH = "D&C0004010I".getBytes();
	private static final byte[] CMD_CLOSE_SWITCH = "D&C0004010J".getBytes();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_debit_credit_read_card);

		//初始化Activity
		InitActivity();

		//数据初始化
		DataInit();

		//初始化IC卡通讯
		InitIccCom();
		//开串口
		IcInterface.openSerialPort();
	}
	@Override
	protected void onResume() {
		IcInterface.write(CMD_OPEN_SWITCH);
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		IcInterface.write(CMD_CLOSE_SWITCH);
		IcInterface.closeSerialPort();
		super.onDestroy();
	}
	//初始化Activity
	public void InitActivity() {

		m_iProcState = 0;

		TermInterface.SetContext(this);

		m_activityMain = this;

		m_textViewScreenShow = (TextView) findViewById(R.id.screen_show);

		((ImageView) findViewById(R.id.image_back)).setOnClickListener(this);
		((Button) findViewById(R.id.trans_btn)).setOnClickListener(this);
		((Button) findViewById(R.id.clear_btn)).setOnClickListener(this);

		m_textViewScreenShow.setText("");

		m_astrScreenShow = new String[SCREEN_SHOW_LINE_MAX];
	}

	//初始化IC卡通讯
	public void InitIccCom() {

		if (DefineFinal.getIccInterface() == 0) {

			//初始化NFC接口
			InitNfcInterface(this);
		}
	}

	//重写onClick事件
	@Override
	public void onClick(View view) {

		int iRet;

		switch (view.getId()) {
		case R.id.image_back:
			finish();
			break;

		case R.id.trans_btn:

			//借贷记读卡
			DebitCreditDemo();

			break;

		case R.id.clear_btn:
			m_textViewScreenShow.setText("");
			break;

		default:
			break;
		}
	}

	//借贷记Demo
	public void DebitCreditDemo() {

		if (m_iProcState == 1) {
			Toast.makeText(this, getResources().getString(R.string.card_being_dealt_with), Toast.LENGTH_SHORT).show();
			return;
		}

		m_iProcState = 1;

		if (DefineFinal.getIccInterface() == 0) {
			//Android Nfc
			NfcReader.ClostConnect();
		}

		//显示屏信息处理
		ScreenShowInfo screenShowInfo = new ScreenShowInfo();
		screenShowInfo.setClearLine(255);
		screenShowInfo.setShowLine(2);
		screenShowInfo.setShowInfo(getResources().getString(R.string.card_insert_contact_card));
		screenShowInfo.setAlignMode(ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
		ShowScreenInfoProc(screenShowInfo);

		Thread thread = new Thread(new Runnable() {

			public Handler mHandler;

			@Override
			public void run() {

				Looper.prepare();

				mHandler = new Handler() {
					public void handleMessage(Message msg) {
						// process incoming messages here
					}
				};

				int iLine = 1;
				Message message = null;

				try {
					if (PbocEmvInterface.PbocEmvDebitCreditReadCard() != 0) {
						return;
					}

					ReturnStringData returnStringData = new ReturnStringData();

					ScreenShowInfo screenShowInfo = new ScreenShowInfo();
					screenShowInfo.setClearLine(255);
					screenShowInfo.setShowLine(iLine++);
					screenShowInfo.setShowInfo(getResources().getString(R.string.card_read_success));
					screenShowInfo.setAlignMode(ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);

					message = null;
					message = new Message();
					message.what = 0xF1;
					message.obj = screenShowInfo;
					m_ActivityHandler.sendMessage(message);

					if (PbocEmvInterface.FindAllTag(0x5A, returnStringData) == 0) {
//					if (PbocEmvInterface.FindAllTag(0x0084000008, returnStringData) == 0) {
						Log.d("jokey", "卡号 : " + returnStringData.getStringData());
						screenShowInfo = new ScreenShowInfo();
						screenShowInfo.setClearLine(0);
						screenShowInfo.setShowLine(iLine++);
						screenShowInfo.setShowInfo(getResources().getString(R.string.card_card_number) + returnStringData.getStringData());
						screenShowInfo.setAlignMode(ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);

						message = null;
						message = new Message();
						message.what = 0xF1;
						message.obj = screenShowInfo;
						m_ActivityHandler.sendMessage(message);
					}

					if (PbocEmvInterface.FindAllTag(0x57, returnStringData) == 0) {
						Log.d("jokey", "二磁道 : " + returnStringData.getStringData());
						screenShowInfo = new ScreenShowInfo();
						screenShowInfo.setClearLine(0);
						screenShowInfo.setShowLine(iLine++);
						screenShowInfo.setShowInfo(getResources().getString(R.string.card_second_track) + returnStringData.getStringData());
						screenShowInfo.setAlignMode(ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);

						message = null;
						message = new Message();
						message.what = 0xF1;
						message.obj = screenShowInfo;
						m_ActivityHandler.sendMessage(message);
					}

					if (PbocEmvInterface.FindAllTag(0x5F20, returnStringData) == 0) {
						Log.d("jokey", "名字 : " + returnStringData.getStringData());
						screenShowInfo = new ScreenShowInfo();
						screenShowInfo.setClearLine(0);
						screenShowInfo.setShowLine(iLine++);
						screenShowInfo.setShowInfo(getResources().getString(R.string.card_user_name) + new String(MyUtil.HexStringToByteArray(returnStringData.getStringData()), "gbk"));
						screenShowInfo.setAlignMode(ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);

						message = null;
						message = new Message();
						message.what = 0xF1;
						message.obj = screenShowInfo;
						m_ActivityHandler.sendMessage(message);
					}
					if(iLine == 2){
						screenShowInfo.setClearLine(255);
						screenShowInfo.setShowLine(iLine++);
						screenShowInfo.setShowInfo(getResources().getString(R.string.card_read_fail));
						screenShowInfo.setAlignMode(ScreenShowInfo.SCREEN_SHOW_ALIGN_CENTER);
	
						message = null;
						message = new Message();
						message.what = 0xF1;
						message.obj = screenShowInfo;
						m_ActivityHandler.sendMessage(message);
					}
					m_iProcState = 0;

					return;
				} catch (Exception e) {
					
					e.printStackTrace();
				} finally {
				}

				Looper.loop();
			}
		});

		thread.start();
	}

	//NFC读写处理
	public void NfcReadWriteProc() {

		//IC卡处理
		//DebitCreditDemo();
	}

	public Handler m_ActivityHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				break;

			case 0xF1:

				//显示屏信息
				ScreenShowInfo scrnShowInfo = (ScreenShowInfo) msg.obj;

				//显示屏信息处理
				ShowScreenInfoProc(scrnShowInfo);

				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	//数据初始化
	public void DataInit() {

		if (MyUtil.GetSysConfig((Activity) this) != 0) {
			Toast.makeText(this, getResources().getString(R.string.card_no_configuration_info), Toast.LENGTH_SHORT).show();
		}
	}

	//显示屏信息处理
	public void ShowScreenInfoProc(ScreenShowInfo i_sScreenShowInfo) {

		int i;

		if (i_sScreenShowInfo.getClearLine() == 255) {
			for (i = 0; i < SCREEN_SHOW_LINE_MAX; i++) {
				SetLineSpaceInfo(i);
			}
		} else if (i_sScreenShowInfo.getClearLine() <= SCREEN_SHOW_LINE_MAX && i_sScreenShowInfo.getClearLine() > 0) {
			SetLineSpaceInfo(i_sScreenShowInfo.getClearLine() - 1);
		}

		String strShowInfo = "";
//		if (i_sScreenShowInfo.getShowInfo().length() > SCREEN_SHOW_COL_MAX) {
//			strShowInfo = i_sScreenShowInfo.getShowInfo().substring(0, SCREEN_SHOW_COL_MAX);
//		} else {
		strShowInfo = i_sScreenShowInfo.getShowInfo();
//		}

		m_astrScreenShow[i_sScreenShowInfo.getShowLine() - 1] = strShowInfo;

		String strScreenInfo = "";
		for (i = 0; i < SCREEN_SHOW_LINE_MAX; i++) {
			strScreenInfo += m_astrScreenShow[i] + "\r\n";
		}

		m_textViewScreenShow.setText(strScreenInfo);
	}

	//置空行信息
	public void SetLineSpaceInfo(int i_iLineNum) {

		int i;

		m_astrScreenShow[i_iLineNum] = "";

		for (i = 0; i < SCREEN_SHOW_COL_MAX; i++) {
			m_astrScreenShow[i_iLineNum] += " ";
		}
	}
}
