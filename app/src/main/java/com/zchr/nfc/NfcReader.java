package com.zchr.nfc;

import java.io.IOException;
import java.util.Arrays;

import com.zchr.util.DebugLog;

import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;

public class NfcReader {

	//成功
	public static final int CARD_READER_SUCCESS = 0x00;

	//失败
	public static final int CARD_READER_ERROR = 0x01;

	//数据不正确
	public static final int CARD_READER_DATA_ERROR = 0x02;

	//发生异常
	public static final int CARD_READER_EXCEPTION = 0x03;

	//NFC句柄 
	private static IsoDep m_isoDep = null;

	//NFC Tech list
	public static String[][] m_astrTechlists;

	//Intent Filter
	public static IntentFilter[] m_aItentFilter;

	//错误信息
	public static String m_strErrMsg = "";

	//设置DEP
	public static int SetDep(IsoDep isoDep) {

		m_isoDep = isoDep;
		return CARD_READER_SUCCESS;
	}

	//连接卡
	public static int ConnectCard() {

		try {
			if (m_isoDep != null ? !m_isoDep.isConnected() : false) {
				m_isoDep.close();
				m_isoDep.connect();
			} else {
				return CARD_READER_ERROR;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DebugLog.Log("连接卡IO异常,%s\r\n", e.toString());
			return CARD_READER_ERROR;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DebugLog.Log("连接卡异常,%s\r\n", e.toString());
			return CARD_READER_ERROR;
		}

		return CARD_READER_SUCCESS;
	}

	//发送APDU,Byte形式
	public static int SendApdu(byte[] byteSendApdu, int iSendApduLen, ISO7816 iso7816ApduRsp) {

		if (m_isoDep == null) {
			m_strErrMsg = "isoDep is null";
			return CARD_READER_DATA_ERROR;
		}

		if (iso7816ApduRsp == null) {
			m_strErrMsg = "iso7816ApduRsp is null";
			return CARD_READER_DATA_ERROR;
		}

		try {

			byte[] byteApdu = Arrays.copyOfRange(byteSendApdu, 0, iSendApduLen);

			byte[] byteResult = m_isoDep.transceive(byteApdu);

			if (iso7816ApduRsp.parseRspData(byteResult, (short) byteResult.length) != ISO7816.ISO7816_APDU_RSP_SUCCESS) {
				m_strErrMsg = iso7816ApduRsp.getStrErrMsg();
				return CARD_READER_ERROR;
			}
		} catch (IOException e) {
			m_strErrMsg = "SendApdu exception : " + e.toString();
			return CARD_READER_EXCEPTION;
		}

		return CARD_READER_SUCCESS;
	}

	//发送APDU,String形式
	public static int SendApdu(String strApdu, ISO7816 iso7816ApduRsp) {

		if (strApdu.length() < 10) {

			m_strErrMsg = String.format("strApdu length %d error", strApdu.length());
			return CARD_READER_DATA_ERROR;
		}

		try {

			byte[] byteApdu = HexStringToByteArray(strApdu);

			int iRet = SendApdu(byteApdu, byteApdu.length, iso7816ApduRsp);
			if (iRet != CARD_READER_SUCCESS) {
				return iRet;
			}
		} catch (Exception e) {
			m_strErrMsg = String.format("SendApdu exception : %s", e.toString());
			return CARD_READER_EXCEPTION;
		}

		return CARD_READER_SUCCESS;
	}

	//关闭连接
	public static int ClostConnect() {
		try {
			if (m_isoDep != null) {
				m_isoDep.close();
				m_isoDep = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return CARD_READER_ERROR;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return CARD_READER_ERROR;
		}

		return CARD_READER_SUCCESS;
	}

	//获取错误信息
	public static final String getStrErrMsg() {
		return m_strErrMsg;
	}

	static {

		try {
			// the tech lists used to perform matching for dispatching of the
			// ACTION_TECH_DISCOVERED intent
			m_astrTechlists = new String[][] { { IsoDep.class.getName() }, { NfcV.class.getName() }, { NfcF.class.getName() }, { MifareClassic.class.getName() }, };

			m_aItentFilter = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*") };
		} catch (Exception e) {
			m_strErrMsg = "Error communicating with card: " + e.toString();
		}
	}

	public static byte[] HexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
