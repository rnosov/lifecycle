package com.zchr.util;

public class SysException extends Exception {

	String m_strEceptionMsg = "";

	public SysException(String strExceptionMsg) {
		// TODO Auto-generated constructor stub
		super(strExceptionMsg);

		m_strEceptionMsg = strExceptionMsg;
	}

	public String toString() {
		return m_strEceptionMsg;
	}
}
