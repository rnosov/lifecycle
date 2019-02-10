package com.zchr.rd.tcpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.zchr.util.DebugLog;

public class TcpClient {

	// 成功
	public static final int TCP_CLIENT_SUCCESS = 0x00;

	// 错误
	public static final int TCP_CLIENT_ERROR = 0x01;

	// 长度超界
	public static final int TCP_CLIENT_LENGTH_TO_MAX = 0x02;

	// 连接超时
	public static final int TCP_CLIENT_CONNECT_TIME_OUT = 0x03;

	// 发送接收失败
	public static final int TCP_CLIENT_SEND_RECV_FAILE = 0x04;

	// 最大收发缓冲长度
	private final int MAX_BUFF_SIZE = 1024 * 30;

	//连接模式,0－长连接、1－短连接
	private int m_iConnectMode;

	//连接状态,0-未连接、1－已连接
	private int m_iConnectState;

	//套接字
	Socket m_socket = null;

	OutputStream ou = null;

	InputStream in = null;

	// 服务端Ip地址
	private String m_strSvrIp;

	// 服务端端口
	private int m_iSvrPort;

	// 超时值,豪秒
	private int m_iTimeOut;

	// 错误信息
	private String m_strErroInfo;

	public TcpClient(String i_strIp, int i_iPort, int i_iTimeOut) {

		m_strSvrIp = i_strIp;
		m_iSvrPort = i_iPort;
		m_iTimeOut = i_iTimeOut;
		m_strErroInfo = "";

		m_iConnectState = 0;

		//默认为短连接
		m_iConnectMode = 1;
	}

	//设置模式
	public void SetConnectMode(int i_iConnentMode) {

		m_iConnectMode = i_iConnentMode;
	}

	//发送
	public int Send(byte[] i_byteSendData, int i_iSendDataOffSet, int i_iSendDataLen) {

		try {

			if (m_iConnectState == 0) {

				DebugLog.Log("Ip : %s Port : %d TimeOut : %d\r\n", m_strSvrIp, m_iSvrPort, m_iTimeOut);

				// 连接服务器 并设置连接超时为5秒
				m_socket = new Socket();
				m_socket.connect(new InetSocketAddress(m_strSvrIp, m_iSvrPort), m_iTimeOut);
				m_iConnectState = 1;
			}

			// 获取输出流
			ou = m_socket.getOutputStream();

			DebugLog.Log("发送数据 : ");
			DebugLog.Log(i_byteSendData, i_iSendDataLen);

			// 向服务器发送信息
			ou.write(i_byteSendData);
			ou.flush();

		} catch (SocketException se) {
			se.printStackTrace();
			DebugLog.Log("通讯异常 : %s\r\n", se.toString());
			m_strErroInfo = "通讯异常";
			return TCP_CLIENT_SEND_RECV_FAILE;
		} catch (SocketTimeoutException se) {
			se.printStackTrace();
			DebugLog.Log("连接超时 : %s\r\n", se.toString());
			m_strErroInfo = "连接超时";
			return TCP_CLIENT_CONNECT_TIME_OUT;
		} catch (IOException e) {
			e.printStackTrace();
			DebugLog.Log("数据发生异常 : %s\r\n", e.toString());
			m_strErroInfo = "数据发生异常";
			return TCP_CLIENT_SEND_RECV_FAILE;
		} catch (Exception e) {
			e.printStackTrace();
			DebugLog.Log("发生未知异常 : %s\r\n", e.toString());
			m_strErroInfo = "发生未知异常";
			return TCP_CLIENT_SEND_RECV_FAILE;
		} finally {
		}

		return TCP_CLIENT_SUCCESS;
	}

	//接收
	public int Recv(RspData io_rspData) {

		try {

			if (m_iConnectState == 0) {

				return TCP_CLIENT_SEND_RECV_FAILE;
			}

			// 获取输入流
			in = m_socket.getInputStream();

			// 接收信息
			byte[] byteRecvData = new byte[MAX_BUFF_SIZE];
			int iRecvLen = in.read(byteRecvData);

			Date dateStart = new SimpleDateFormat("yyyyMMddHHmmssSSS").get2DigitYearStart();

			// 先收长度
			while (iRecvLen < 2) {

				DebugLog.Log("长度字节未够,已接收%d的数据\r\n", iRecvLen);

				// 是否超时
				Date dateEnd = new SimpleDateFormat("yyyyMMddHHmmssSSS").get2DigitYearStart();
				long timeDelta = (dateEnd.getTime() - dateStart.getTime());
				if (timeDelta > m_iTimeOut) {
					m_strErroInfo = "收长度时超时";
					DebugLog.Log("%s\r\n", m_strErroInfo);
					return TCP_CLIENT_SEND_RECV_FAILE;
				}

				iRecvLen += in.read(byteRecvData, iRecvLen, MAX_BUFF_SIZE - iRecvLen);
			}

			// 计算长度
			int iBackLen = (int) ((byteRecvData[0] & 0xFF) * 256 + (int) (byteRecvData[1] & 0xFF));

			// 是否超过最大接收长度
			if (iBackLen + 2 > MAX_BUFF_SIZE) {
				return 1;
			}

			DebugLog.Log("需要接收%d\r\n", iBackLen + 2);

			dateStart = new SimpleDateFormat("yyyyMMddHHmmssSSS").get2DigitYearStart();

			// 继续接收后续数据
			while (iRecvLen < iBackLen + 2) {

				//DebugLog.Log("已接收%d的数据\r\n", iRecvLen);

				// 是否超时
				Date dateEnd = new SimpleDateFormat("yyyyMMddHHmmssSSS").get2DigitYearStart();
				long timeDelta = (dateEnd.getTime() - dateStart.getTime());
				if (timeDelta > m_iTimeOut) {
					m_strErroInfo = "收数据时超时";
					DebugLog.Log("%s\r\n", m_strErroInfo);
					return TCP_CLIENT_SEND_RECV_FAILE;
				}

				int iRecvLenTemp = in.read(byteRecvData, iRecvLen, MAX_BUFF_SIZE - iRecvLen);

				if (iRecvLenTemp <= 0) {
					continue;
				} else {
					iRecvLen += iRecvLenTemp;
				}
			}

			DebugLog.Log("接收完毕,%d\r\n", iRecvLen);

			// 接收完毕
			io_rspData.setRspData(byteRecvData);
			io_rspData.setRspDataLen(iBackLen + 2);

		} catch (SocketException se) {
			se.printStackTrace();
			DebugLog.Log("通讯异常 : %s\r\n", se.toString());
			m_strErroInfo = "通讯异常";
			return TCP_CLIENT_SEND_RECV_FAILE;
		} catch (SocketTimeoutException se) {
			se.printStackTrace();
			DebugLog.Log("连接超时 : %s\r\n", se.toString());
			m_strErroInfo = "连接超时";
			return TCP_CLIENT_CONNECT_TIME_OUT;
		} catch (IOException e) {
			e.printStackTrace();
			DebugLog.Log("数据发生异常 : %s\r\n", e.toString());
			m_strErroInfo = "数据发生异常";
			return TCP_CLIENT_SEND_RECV_FAILE;
		} catch (Exception e) {
			e.printStackTrace();
			DebugLog.Log("发生未知异常 : %s\r\n", e.toString());
			m_strErroInfo = "发生未知异常";
			return TCP_CLIENT_SEND_RECV_FAILE;
		} finally {

			if (m_iConnectMode == 1 ? m_socket != null : false) {

				//关闭套接字
				CloseSocket();
			}
		}

		return TCP_CLIENT_SUCCESS;
	}

	// 发送接收
	public int SendAndRecv(byte[] i_byteSendData, int i_iSendDataOffSet, int i_iSendDataLen, RspData io_rspData) {

		int iRet;

		iRet = Send(i_byteSendData, i_iSendDataOffSet, i_iSendDataLen);
		if (iRet != TCP_CLIENT_SUCCESS) {
			return iRet;
		}

		iRet = Recv(io_rspData);
		if (iRet != TCP_CLIENT_SUCCESS) {
			return iRet;
		}

		return TCP_CLIENT_SUCCESS;
	}

	//关闭套接字
	public void CloseSocket() {

		if (ou != null) {

			// 关闭输出流
			try {
				ou.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (in != null) {

			// 关闭输入流
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (m_socket != null) {
			// 关闭套接字
			try {
				m_socket.close();
				m_socket = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 获取错误信息
	public String GetErrorInfo() {

		return m_strErroInfo;
	}
}
