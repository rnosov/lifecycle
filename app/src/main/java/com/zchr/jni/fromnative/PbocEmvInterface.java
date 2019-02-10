package com.zchr.jni.fromnative;

import com.zchr.util.ReturnStringData;

public class PbocEmvInterface {

	static private final String libSoName = "PbocEmvInterface";

	// Test Demo
	static public native int TestDemo();

	// 借/贷记
	static public native int PbocEmvDebitCredit(int i_iTransAmt, int i_iOtherAmt, int i_iTransType);

	// 借/贷记读卡
	static public native int PbocEmvDebitCreditReadCard();

	//查找所有Tag
	static public native int FindAllTag(int iFindTag, ReturnStringData io_returnStringData);

	//载入JNI生成的so库文件
	static {
		System.loadLibrary(libSoName);
	}
}
