package com.authentication.activity;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.authentication.utils.DataUtils;
import com.authentication.utils.ToastUtil;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
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
import android_serialport_api.ICCardAPI;
import android_serialport_api.LooperBuffer;
import android_serialport_api.SerialPortManager;

public class ICCardActivity extends BaseActivity implements OnClickListener {

	private Button mBtnClean, mBtnGetRandom, mBtnSwitch, mBtnCycleGetRandom,
			mBtnClose, mBtnCleanCount;
	private static EditText mRecvContent;
	private static TextView mShowCount;
	private static EditText mEdSend;
	private Button mSend, mClear;
	private Spinner mSpinner;
	private static String ret;
	static ICCardAPI api;
	private static String TAG = "jokey";
	private boolean isStop = false;
	private ListenThread mListenThread = null;
	private static boolean isCycle;
	protected static ExecutorService pool;
	private static int count = 0;
	private WakeLock wakeLock = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_iccard);
		initView();
		initViewListener();
		initData();
	}

	private void initView() {
		mBtnClean = (Button) findViewById(R.id.btn_clean);
		mBtnGetRandom = (Button) findViewById(R.id.btn_GetRandom);
		mRecvContent = (EditText) findViewById(R.id.show_content);
		mBtnSwitch = (Button) findViewById(R.id.btn_switch);
		mBtnCycleGetRandom = (Button) findViewById(R.id.btn_CycleGetRandom);
		mBtnClose = (Button) findViewById(R.id.btn_CloseCycle);
		mShowCount = (TextView) findViewById(R.id.show_count);
		mBtnCleanCount = (Button) findViewById(R.id.btn_cleanCount);

		mEdSend = (EditText) findViewById(R.id.send_content);
		mSend = (Button) findViewById(R.id.send_btn);
		mClear = (Button) findViewById(R.id.clear_btn);

		mSpinner = (Spinner) findViewById(R.id.spinner);
	}

	private void initViewListener() {
		mBtnClean.setOnClickListener(this);
		mBtnGetRandom.setOnClickListener(this);
		mBtnSwitch.setOnClickListener(this);
		mBtnCycleGetRandom.setOnClickListener(this);
		mBtnClose.setOnClickListener(this);
		mBtnCleanCount.setOnClickListener(this);
		mSend.setOnClickListener(this);
		mClear.setOnClickListener(this);

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					mEdSend.setText("");
					break;
				case 1:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_DELETE_FILE));
					break;
				case 2:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_CREATE_MF));
					break;
				case 3:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_END_CREATE_MF));
					break;
				case 4:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_CREATE_DF));
					break;
				case 5:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_END_CREATE_DF));
					break;
				case 6:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_CREATE_BINARY));
					break;
				case 7:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_CHOOSE_BINARY));
					break;
				case 8:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_READ_BINARY));
					break;
				case 9:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_WRITE_BINARY));
					break;
				case 10:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_PROC_DIR));
					break;
				case 11:
					mEdSend.setText(DataUtils
							.toHexString(ICCardAPI.CMD_Get_Challenge));
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void initData() {
		mRecvContent.setText("");
		api = new ICCardAPI();
		pool = Executors.newSingleThreadExecutor();
		ret = "";
		isCycle = false;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.ic_cmd_value));
		mSpinner.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_clean:
			mRecvContent.setText("");
			ret = "";
			break;
		case R.id.btn_cleanCount:
			count = 0;
			mShowCount.setText(getResources().getString(R.string.ic_show_count)
					+ count);
			break;
		case R.id.btn_switch:
			new Thread() {
				@Override
				public void run() {
					if (SerialPortManager.getInstance().isOpen()) {
						switchStatus();
					}
				}
			}.start();
			break;
		case R.id.btn_GetRandom:
			isCycle = false;
			pool.execute(task);
			break;
		case R.id.btn_CycleGetRandom:
			isCycle = true;
			mBtnGetRandom.setEnabled(false);
			mBtnSwitch.setEnabled(false);
			mBtnCycleGetRandom.setEnabled(false);
			pool.execute(task);
			break;
		case R.id.btn_CloseCycle:
			isCycle = false;
			mBtnGetRandom.setEnabled(true);
			mBtnSwitch.setEnabled(true);
			mBtnCycleGetRandom.setEnabled(true);
			break;
		case R.id.send_btn:
			boolean isExit = false;
			if (!SerialPortManager.getInstance().isOpen()
					&& !SerialPortManager.getInstance().openSerialPort()) {
				ToastUtil.showToast(this, R.string.open_serial_fail);
				isExit = true;
			}
			if (isExit) {
				return;
			}
			if (TextUtils.isEmpty(mEdSend.getText())) {
				ToastUtil.showToast(this, R.string.ic_not_empty);
				return;
			}
			if (!isHexStrNumber(mEdSend.getText().toString())) {
				ToastUtil.showToast(this, R.string.ic_not_valid);
				return;
			}

			final byte[] bOutArray = HexToByteArr(mEdSend.getText().toString());
			new Thread() {
				@Override
				public void run() {
					api.sendCommand(bOutArray);
				}
			}.start();
			break;
		case R.id.clear_btn:
			mEdSend.setText("");
			break;
		}
	}

	public static boolean isHexStrNumber(String s) {
		Matcher m = Pattern.compile("^[0-9A-Fa-f]+$").matcher(s);
		return m.matches();
	}

	static public byte HexToByte(String inHex)// Hex字符串转byte
	{
		return (byte) Integer.parseInt(inHex, 16);
	}

	// 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
	static public int isOdd(int num) {
		return num & 0x1;
	}

	// 转hex字符串转字节数组
	static public byte[] HexToByteArr(String inHex)// hex字符串转字节数组
	{
		int hexlen = inHex.length();
		byte[] result;
		if (isOdd(hexlen) == 1) {// 奇数
			hexlen++;
			result = new byte[(hexlen / 2)];
			inHex = "0" + inHex;
		} else {// 偶数
			result = new byte[(hexlen / 2)];
		}
		int j = 0;
		for (int i = 0; i < hexlen; i += 2) {
			result[j] = HexToByte(inHex.substring(i, i + 2));
			j++;
		}
		return result;
	}

	private boolean switchStatus() {
		// 读取切换
		boolean ret = false;
		ret = api.switchStatus();
		String tmp = "\r\n" + getResources().getString(R.string.ic_switch_cmd);
		if (ret) {
			ret = true;
			tmp += getResources().getString(R.string.ic_success);
		} else
			tmp += getResources().getString(R.string.ic_failure);
		mHandler.obtainMessage(0, tmp).sendToTarget();
		return ret;
	}

	private static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				mShowCount.setText("" + R.string.ic_show_count + count);
				if (mRecvContent.getLineCount() > 100)// 100行 清空
				{
					ret = "";
					mRecvContent.setText("");
				}
				ret += (String) msg.obj;
				mRecvContent.setText(ret);
				mRecvContent.setMovementMethod(ScrollingMovementMethod
						.getInstance());
				mRecvContent.setSelection(mRecvContent.getText().length(),
						mRecvContent.getText().length());
				break;
			}
		}
	};

	protected void onDestroy() {
		api.CloseSwitchStatus();
		SerialPortManager.getInstance().closeSerialPort();
		isStop = true;
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		api.CloseSwitchStatus();
		SerialPortManager.getInstance().closeSerialPort();
		SerialPortManager.getInstance().setLoopBuffer(null);
		isStop = true;
		releaseWakeLock();
		super.onPause();
	}

	@Override
	protected void onResume() {
		SerialPortManager.getInstance().setLoopBuffer(looperBuffer);
		isStop = false;
		mListenThread = new ListenThread();
		mListenThread.start();
//		api.switchStatus();
		// 获取锁，保持屏幕亮度
//		acquireWakeLock();
		super.onResume();
	}

//	private void acquireWakeLock() {
//		if (wakeLock == null) {
//			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//			wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this
//					.getClass().getCanonicalName());
//			wakeLock.acquire();
//		}
//
//	}

	private void releaseWakeLock() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}

	}

	class ListenThread extends Thread {
		public void run() {
			isStop = false;
			mHandler.obtainMessage(0,
					"\r\n" + getResources().getString(R.string.ic_start_thread))
					.sendToTarget();
			while (!isStop) {
				looperBuffer.getFullPacket();
				SystemClock.sleep(100);
			}
		}
	}

	private LooperBuffer looperBuffer = new LoopBufferIC();

	public static class LoopBufferIC implements LooperBuffer {
		private Queue<byte[]> QueueList = new LinkedList<byte[]>();
		private byte[] ackCode = { 0x3B, 0x6C, 0x00, 0x02, (byte) 0x86, 0x38,
				0x4f };
		final byte[] ackCodeByRandom = { (byte) 0x90, 0x00 };

		public synchronized void add(byte[] buf) {
			QueueList.add(buf);
		}

		public synchronized byte[] getFullPacket() {
			byte[] tmpBuf;
			if ((tmpBuf = QueueList.poll()) != null) {
				// MyApplication.logger.debug(DataUtils.toHexString(tmpBuf));
				Log.i(TAG, DataUtils.toHexString(tmpBuf));
				// 卡复位反馈信息: cadf0035103b6c00025101863800000000000035a3e3
				if (tmpBuf.length > 14) {
					if (tmpBuf[5] == ackCode[0] && tmpBuf[6] == ackCode[1]
							&& tmpBuf[7] == ackCode[2]
							&& tmpBuf[8] == ackCode[3]
							&& tmpBuf[11] == ackCode[4]
							&& tmpBuf[12] == ackCode[5]) {
						if ((tmpBuf[10] & 0x9F) == tmpBuf[10]
								|| (tmpBuf[10] & 0x6F) == tmpBuf[10]) {
							mHandler.obtainMessage(0,
									"\r\n" + R.string.ic_init_success)
									.sendToTarget();
						} else
							mHandler.obtainMessage(0,
									"\r\n" + R.string.ic_init_failure)
									.sendToTarget();
						return null;
					}
				} else if (tmpBuf.length == 1 && tmpBuf[0] == ackCode[6]) {
					mHandler.obtainMessage(0,
							"\r\n" + R.string.ic_switch_success).sendToTarget();
					return null;
				}

				if (isCycle == true && tmpBuf.length > 7
						&& tmpBuf[tmpBuf.length - 3] == ackCodeByRandom[0]
						&& tmpBuf[tmpBuf.length - 2] == ackCodeByRandom[1]) {
					count++;
					mHandler.obtainMessage(
							0,
							"\r\n" + R.string.ic_get_random_success
									+ DataUtils.toHexString(tmpBuf))
							.sendToTarget();
					if (isCycle)
						pool.execute(task);
					return null;
				}
				mHandler.obtainMessage(0,
						"\r\n" + DataUtils.toHexString(tmpBuf)).sendToTarget();
				return tmpBuf;
			}
			return null;
		}
	}

	private static Runnable task = new Runnable() {
		@Override
		public void run() {
			byte[] CMD_Get_Challenge = { 0x00, (byte) 0x84, 0x00, 0x00, 0x08 };
			SerialPortManager.getInstance().write(
					api.makeProcData(CMD_Get_Challenge));
		}
	};
}