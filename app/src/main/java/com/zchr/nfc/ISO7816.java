package com.zchr.nfc;

import java.util.Arrays;
import android.util.Log;

public class ISO7816 {

	/** 成功 */
	public static final int ISO7816_APDU_RSP_SUCCESS = 0x00;

	/** 失败 */
	public static final int ISO7816_APDU_RSP_ERROR = 0x01;

	/** 数据不正确 */
	public static final int ISO7816_APDU_RSP_DATA_ERROR = 0x02;

	/** 发生异常 */
	public static final int ISO7816_APDU_RSP_EXCEPTION = 0x03;

	/** ISO-DEP command HEADER for selecting an AID */
	public static final String SELECT_APDU_HEADER = "00A40400";

	/** "OK" status word sent in response to SELECT AID command (0x9000) */
	public static final byte[] SW_OK = { (byte) 0x90, (byte) 0x00 };

	/** LOG 标识 */
	private static final String TAG = "ISO7816Apdu";

	private int iRspDataLen;
	private byte[] byteRspData;
	private byte[] byteSW;
	private String strErrMsg;

	public ISO7816() {
		// TODO Auto-generated constructor stub
		iRspDataLen = 0;
		byteRspData = new byte[256];
		byteSW = new byte[2];
		strErrMsg = "";
	}

	public int parseRspData(byte[] byteRspData, short sRspDataLen) {

		iRspDataLen = 0;

		if (sRspDataLen < 2) {
			strErrMsg = "sRspDataLen [" + sRspDataLen + "]" + "不正确";
			Log.i(TAG, strErrMsg);
			return ISO7816_APDU_RSP_DATA_ERROR;
		}

		try {

			if (byteRspData.length < 2) {
				strErrMsg = "byteRspData.length [" + byteRspData.length + "]" + "不正确";
				Log.i(TAG, strErrMsg);
				return ISO7816_APDU_RSP_DATA_ERROR;
			}

			if (sRspDataLen > 2) {
				this.byteRspData = Arrays.copyOfRange(byteRspData, 0, sRspDataLen - 2);
			} else {
				this.byteRspData = null;
			}

			iRspDataLen = (int) (sRspDataLen - 2);

			byteSW = Arrays.copyOfRange(byteRspData, sRspDataLen - 2, sRspDataLen);
		} catch (Exception e) {
			strErrMsg = "parseRspData Exception " + e.toString();
			Log.i(TAG, strErrMsg);
			return ISO7816_APDU_RSP_EXCEPTION;
		}

		return ISO7816_APDU_RSP_SUCCESS;
	}

	public int getiRspDataLen() {
		return iRspDataLen;
	}

	public byte[] getByteRspData() {
		return byteRspData;
	}

	public byte[] getSW() {
		return byteSW;
	}

	public String getStrErrMsg() {
		return strErrMsg;
	}

	public static String ParseSW(byte[] byteSW) {

		String str = "";
		short stSw = (short) ((short) (((short) byteSW[0]) << 8) + (short) byteSW[1]);

		switch (stSw) {
		case 0x6A82:
			str = "该应用未找到";
			break;

		default:
			str = "未知状态";
			break;
		}

		return str;
	}
}
