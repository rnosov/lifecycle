package com.zchr.jni.tonative;

import java.util.ArrayList;
import java.util.HashMap;

import com.zchr.util.InputData;
import com.zchr.util.ListItemInfoSelect;
import com.zchr.util.MsgBox;
import com.zchr.util.MsgConfirm;
import com.zchr.util.ReturnIntData;
import com.zchr.util.ReturnStringData;
import com.zchr.util.ScreenShowInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

public class TermInterface extends Activity {

	//Activity上下文
	static public Activity m_activityMain = null;

	//Activity Handler
	static public Handler m_activityHandler = null;

	//设置上下文
	static public void SetContext(Activity i_activity) {
		m_activityMain = i_activity;
	}

	//设置消息Handler
	static public void SetHandler(Handler i_handler) {
		m_activityHandler = i_handler;
	}

	//信息提示
	static public int MsgBoxShow(String i_strTitle, String i_strMsg) {

		MsgBox msgBox = new MsgBox(m_activityMain);

		int iRet = msgBox.showDialog(i_strTitle, i_strMsg);

		return iRet;
	}

	//信息确认
	static public int MsgConfirmShow(String i_strTitle, String i_strMsg, String i_strLeftBtnShow, String i_strRightBtnShow) {

		MsgConfirm msgConfirm = new MsgConfirm(m_activityMain, i_strLeftBtnShow, i_strRightBtnShow);

		int iRet = msgConfirm.showDialog(i_strTitle, i_strMsg);

		return iRet;
	}

	//输入明文密码
	static public int InputPlantPin(String i_strTitle, ReturnStringData io_returnPin) {

		int[] aiCntRange = new int[2];

		aiCntRange[0] = 6;
		aiCntRange[1] = 6;

		InputData inputData = new InputData(m_activityMain, aiCntRange, "TYPE_PIN");

		int iRet = inputData.showDialog(i_strTitle, io_returnPin);

		return iRet;
	}

	//列表选择
	static public int ListSelect(String i_strTitle, String[] i_astrListData, ReturnIntData io_returnSelect) {

		ListItemInfoSelect listSelect = new ListItemInfoSelect(m_activityMain, i_astrListData);

		int iRet = listSelect.showDialog(i_strTitle, io_returnSelect);

		return iRet;
	}

	//屏幕显示
	static public int ScreenShow(int i_iClearLine, int i_iShowLine, int i_iAlignMode, String i_strShowInfo) {

		ScreenShowInfo screenShowInfor = new ScreenShowInfo();
		screenShowInfor.setClearLine(i_iClearLine);
		screenShowInfor.setShowLine(i_iShowLine);
		screenShowInfor.setAlignMode(i_iAlignMode);
		screenShowInfor.setShowInfo(i_strShowInfo);

		if (m_activityHandler != null) {

			Message message;
			message = null;
			message = new Message();
			message.what = 0xF1;
			message.obj = screenShowInfor;
			m_activityHandler.sendMessage(message);
		}

		return 0;
	}
}
