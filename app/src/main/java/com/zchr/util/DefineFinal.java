package com.zchr.util;

public class DefineFinal {

	//服务器IP
	//public static String m_strServerIp = "218.108.6.187";
	public static String m_strServerIp = "192.168.1.100";

	//服务器端口
	public static int m_iServerPort = 5088;

	//网络超时值,毫秒
	public static int m_iNetTimeOut = 5000;

	//读卡超时值
	public static int m_iReadCardTimeOut;

	//IC卡接口,0-android NFC、1－CPOS800 接触式
	public static int iIccInterface;

	public static String getServerIp() {
		return m_strServerIp;
	}

	public static void setServerIp(String m_strServerIp) {
		DefineFinal.m_strServerIp = m_strServerIp;
	}

	public static int getServerPort() {
		return m_iServerPort;
	}

	public static void setServerPort(int m_iServerPort) {
		DefineFinal.m_iServerPort = m_iServerPort;
	}

	public static int getIccInterface() {
		return iIccInterface;
	}

	public static int getNetTimeOut() {
		return m_iNetTimeOut;
	}

	public static void setNetTimeOut(int iTimeOut) {
		m_iNetTimeOut = iTimeOut;
	}

	public static int getReadCardTimeOut() {
		return m_iReadCardTimeOut;
	}

	public static void setReadCardTimeOut(int iTimeOut) {
		m_iReadCardTimeOut = iTimeOut;
	}

	public static void setIccInterface(int iIccInterface) {
		DefineFinal.iIccInterface = iIccInterface;
	}
}
