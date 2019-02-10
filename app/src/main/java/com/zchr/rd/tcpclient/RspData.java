package com.zchr.rd.tcpclient;

public class RspData {

	private byte[] m_byteRspData;

	private int m_iRspDataLen;

	public RspData() {

	}

	public void setRspData(byte[] m_byteRspData) {
		this.m_byteRspData = m_byteRspData;
		this.m_iRspDataLen = m_byteRspData.length;
	}

	public void setRspDataLen(int m_iRspDataLen) {
		this.m_iRspDataLen = m_iRspDataLen;
	}

	public byte[] getRspData() {
		return m_byteRspData;
	}

	public int getRspDataLen() {
		return m_iRspDataLen;
	}
}
