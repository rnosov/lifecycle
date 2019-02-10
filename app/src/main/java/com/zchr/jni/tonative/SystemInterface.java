package com.zchr.jni.tonative;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import com.zchr.rd.tcpclient.RspData;
import com.zchr.rd.tcpclient.TcpClient;
import com.zchr.util.DefineFinal;
import com.zchr.util.InputData;
import com.zchr.util.ListItemInfoSelect;
import com.zchr.util.MsgBox;
import com.zchr.util.MsgConfirm;
import com.zchr.util.ReturnIntData;
import com.zchr.util.ReturnStringData;

import android.R.array;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

public class SystemInterface extends Activity {

	//TCP通讯
	static public TcpClient m_tcpClient = null;

	//获取日期
	static public int GetDate(ReturnStringData io_sRetStrData) {

		Date nowDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String strDate = dateFormat.format(nowDate);
		io_sRetStrData.setStringData(strDate);

		return 0;
	}

	//获取时间
	static public int GetTime(ReturnStringData io_sRetStrData) {

		Date nowDate = new Date();
		SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
		String strTime = timeFormat.format(nowDate);
		io_sRetStrData.setStringData(strTime);

		return 0;
	}

	//获取随机数
	static public int GetRnd() {

		int iMax = 255;
		int iMin = 1;

		Random random = new Random();

		int iRnd = random.nextInt(iMax) % (iMax - iMin + 1) + iMin;

		return iRnd;
	}

	//调试信息
	static public void DebugLog(String io_strLog) {

		System.out.printf("%s", io_strLog);
	}

	//打开网络通讯
	static public int OpenNetCom() {

		m_tcpClient = new TcpClient(DefineFinal.getServerIp(), DefineFinal.getServerPort(), DefineFinal.m_iNetTimeOut);

		if (m_tcpClient == null) {
			return 1;
		}

		return 0;
	}

	//网络发送
	static public int NetSendData(byte[] i_abyteSendData, int i_iSendDataLen) {

		if (m_tcpClient == null) {
			return 1;
		}

		byte[] byteSend = new byte[i_iSendDataLen];

		System.arraycopy(i_abyteSendData, 0, byteSend, 0, i_iSendDataLen);

		if (m_tcpClient.Send(i_abyteSendData, 0, i_iSendDataLen) != TcpClient.TCP_CLIENT_SUCCESS) {
			return 2;
		}

		return 0;
	}

	//网络接收
	static public int NetRecvData(RspData io_sRspData) {

		if (m_tcpClient == null) {
			return 1;
		}

		if (m_tcpClient.Recv(io_sRspData) != TcpClient.TCP_CLIENT_SUCCESS) {
			return 2;
		}

		return 0;
	}

	//关闭网络
	static public int CloseNetCom() {

		m_tcpClient.CloseSocket();

		return 0;
	}
}
