package com.zchr.util;

public class SysConfig {

	//服务器IP
	String m_strServerIp;

	//服务器端口
	String m_strServerPort;

	//网络超时值
	int m_iNetTimeOut;

	//读卡超时值
	int m_iReadCardTimeOut;

	//IC卡接口,0-android NFC、1－厂商 RFID
	int m_iIccInterface;

	public SysConfig() {
		// TODO Auto-generated constructor stub
	}

	public String getServerIp() {
		return m_strServerIp;
	}

	public void setServerIp(String m_strIp) {
		this.m_strServerIp = m_strIp;
	}

	public String getServerPort() {
		return m_strServerPort;
	}

	public void setServerPort(String m_strPort) {
		this.m_strServerPort = m_strPort;
	}

	public int getNetTimeOut() {
		return m_iNetTimeOut;
	}

	public void setNetTimeOut(int iTimeOut) {
		this.m_iNetTimeOut = iTimeOut;
	}

	public int getReadCardTimeOut() {
		return m_iReadCardTimeOut;
	}

	public void setReadCardTimeOut(int iTimeOut) {
		this.m_iReadCardTimeOut = iTimeOut;
	}

	public int getIccInterface() {
		return this.m_iIccInterface;
	}

	public void setIccInterface(int iIccInterface) {
		m_iIccInterface = iIccInterface;
	}
}
