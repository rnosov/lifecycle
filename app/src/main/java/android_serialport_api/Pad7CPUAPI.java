package android_serialport_api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android_serialport_api.Pad7PsamAPI.OnA370ResetListener;
import android_serialport_api.Pad7PsamAPI.OnA370SendListener;

public class Pad7CPUAPI {
	/**
	 * SD0 : 0xCA SD1 : 0xDF ID : 0x01, LEN_H : LEN_L :
	 */
	private byte[] CMD_HEAD = { (byte) 0xca, (byte) 0xdf, 0x00, 0x35, 0x00, 0x00 };
	private final byte CMD_END = (byte) 0xe3;
	/**
	 * SD : 0x72 LEN_H : LEN_L :
	 */
	private byte[] SUB_CMD_HEAD = { 0x02, 0x00, 0x00 };
	private final byte SUB_CMD_END = 0x00;
	private final byte[] CMD_RESET = { (byte) 0xCA, (byte) 0xDF, 0x00, 0x35, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00,
			(byte) 0xE3 };
	private final byte[] CMD_GET_RANDOM = { 0x00, (byte) 0x84, 0x00, 0x00, 0x08 };
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private Handler mHandler;
	private byte[] buffer;
	private OnA370ResetListener onA370PsamResetListener;
	private OnA370SendListener onA370PsamSendListener;

	public Pad7CPUAPI() {
		mHandler = new Handler();
	}

	public void open() {
		SerialPortManager.getInstance().openSerialPortIC();
	}

	public void close() {
		SerialPortManager.getInstance().closeSerialPortIC();
	}

	public void reset(OnA370ResetListener onA370PsamResetListener) {
		this.onA370PsamResetListener = onA370PsamResetListener;
		executor.execute(reset);
	}

	public void getRandom(OnA370SendListener onA370PsamSendListener) {
		send(CMD_GET_RANDOM, onA370PsamSendListener);
	}

	public void send(byte[] data, OnA370SendListener onA370PsamSendListener) {
		this.onA370PsamSendListener = onA370PsamSendListener;
		executor.execute(new sendTO(data));
	}

	private Runnable reset = new Runnable() {

		@Override
		public void run() {
			buffer = new byte[100];
			SerialPortManager.getInstance().write(CMD_RESET);
			final int len = SerialPortManager.getInstance().read(buffer, 3000, 100);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					if (len > 0) {
						if (len > 4) {
							byte[] data = new byte[len];
							System.arraycopy(buffer, 0, data, 0, len);
							onA370PsamResetListener.resetSuccess(data);
						} else
							onA370PsamResetListener.resetFailure(OnA370ResetListener.RESET_FAILURE_HAS_DATA);
					} else
						onA370PsamResetListener.resetFailure(OnA370ResetListener.RESET_FAILURE_NO_DATA);
				}
			});
		}
	};

	private class sendTO implements Runnable {
		byte[] cmd;

		sendTO(byte[] cmd) {
			this.cmd = cmd;
		}

		@Override
		public void run() {
			// 拼装指令
			byte[] newCMD = new byte[CMD_HEAD.length + SUB_CMD_HEAD.length + this.cmd.length + 2];
			// 把数据复制进newCMD
			System.arraycopy(CMD_HEAD, 0, newCMD, 0, CMD_HEAD.length);
			System.arraycopy(SUB_CMD_HEAD, 0, newCMD, CMD_HEAD.length, SUB_CMD_HEAD.length);
			System.arraycopy(this.cmd, 0, newCMD, CMD_HEAD.length + SUB_CMD_HEAD.length, this.cmd.length);
			newCMD[newCMD.length - 1] = CMD_END;
			newCMD[newCMD.length - 2] = SUB_CMD_END;
			byte[] subLen = getShort(this.cmd.length);
			byte[] Len = getShort(this.cmd.length + 4);
			newCMD[4] = Len[0];
			newCMD[5] = Len[1];
			newCMD[7] = subLen[0];
			newCMD[8] = subLen[1];
			buffer = new byte[100];
			SerialPortManager.getInstance().write(newCMD);
			final int len = SerialPortManager.getInstance().read(buffer, 3000, 100);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					if (len > 4) {
						byte[] data = new byte[len - 4];
						System.arraycopy(buffer, 3, data, 0, data.length);
						onA370PsamSendListener.receiveData(data);
					} else
						onA370PsamSendListener.failure();
				}
			});
		}
	}

	private byte[] getShort(int len) {
		byte[] targets = new byte[2];
		targets[0] = (byte) (len >> 8 & 0xFF);
		targets[1] = (byte) (len & 0xFF);
		return targets;
	}
}