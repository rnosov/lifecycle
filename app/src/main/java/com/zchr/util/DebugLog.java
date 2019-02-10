package com.zchr.util;

import java.util.Date;
import java.text.SimpleDateFormat;

import android.util.Log;

public class DebugLog {

	public DebugLog() {
		// TODO Auto-generated constructor stub
	}

	public static void Log(String strLog, Object... objLog) {
		Date nowDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDateTime = dateFormat.format(nowDate);
		String StrLogMsg = strDateTime + " : " + String.format(strLog, objLog);
		System.out.printf(strLog, objLog);
	}

	public static void Log(byte[] i_byteData, int i_iDataLen) {
		Date nowDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDateTime = dateFormat.format(nowDate);

		//System.out.printf(strDateTime + " : ");

		for (int i = 0; i < i_iDataLen; i++) {
			System.out.printf("%02X", i_byteData[i]);
		}

		System.out.printf("\r\n");
	}

	public static void Log(String i_strLog, byte[] i_byteData, int i_iDataLen) {
		Date nowDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String strDateTime = dateFormat.format(nowDate);

		//System.out.printf(strDateTime + " : " + i_strLog);

		for (int i = 0; i < i_iDataLen; i++) {
			System.out.printf("%02X", i_byteData[i]);
		}

		System.out.printf("\r\n");
	}
}
