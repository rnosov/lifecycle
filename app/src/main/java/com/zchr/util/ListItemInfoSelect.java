package com.zchr.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.authentication.activity.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ListItemInfoSelect extends Dialog {

	public int dialogResult;

	public Handler mHandler;

	private ListView m_listViewShowInfo;

	public String[] m_astrListData;

	public Context m_conTextMain;

	public int m_iSelect;

	public ListItemInfoSelect(Activity context, String[] i_astrListData) {
		super(context, R.style.dialog_default);
		dialogResult = 0;
		setOwnerActivity(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		m_astrListData = i_astrListData;

		m_conTextMain = context;

		m_iSelect = -1;

		onCreate();
	}

	public void onCreate() {

		setContentView(R.layout.list_select);

		//绑定Layout里面的ListView
		m_listViewShowInfo = (ListView) findViewById(R.id.info_listview);

		//添加点击
		m_listViewShowInfo.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				if (arg2 < 0) {
					return;
				}

				m_iSelect = arg2 + 1;

				endDialog(0);
			}
		});

		//添加长按点击
		m_listViewShowInfo.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			}
		});

		findViewById(R.id.btn_cancle).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View paramView) {

				m_iSelect = -1;

				endDialog(1);
			}
		});
	}

	public int getDialogResult() {
		return dialogResult;
	}

	public void setDialogResult(int dialogResult) {
		this.dialogResult = dialogResult;
	}

	public void endDialog(int result) {
		dismiss();
		setDialogResult(result);
		Message m = mHandler.obtainMessage();
		mHandler.sendMessage(m);
	}

	public int showDialog(String Title, ReturnIntData io_returnIntData) {
		TextView TvErrorInfo = (TextView) findViewById(R.id.title);
		TvErrorInfo.setText(Title);

		ArrayList<HashMap<String, Object>> alMapInfo = new ArrayList<HashMap<String, Object>>();

		int i = 0;
		for (i = 0; i < m_astrListData.length; i++) {

			int iLen = String.format("%d", i + 1).length();

			alMapInfo.add(InitItemInfo(String.format("%d", i + 1), m_astrListData[i]));
		}

		//生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(m_conTextMain, alMapInfo, R.layout.listview_item, new String[] { "ItemTitleLeft", "ItemTextRigth" }, new int[] { R.id.ItemTitleLeft, R.id.ItemTextRigth });

		//添加并且显示
		m_listViewShowInfo.setAdapter(listItemAdapter);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message mesg) {
				throw new RuntimeException();
			}
		};

		super.show();
		try {
			Looper.getMainLooper();
			Looper.loop();
		} catch (RuntimeException e2) {
		}

		io_returnIntData.setIntData(m_iSelect);

		return dialogResult;
	}

	//项初始化
	public HashMap<String, Object> InitItemInfo(String i_strItem, String i_strContext) {

		HashMap<String, Object> mapItem = new HashMap<String, Object>();
		mapItem.put("ItemTitleLeft", i_strItem);
		mapItem.put("ItemTextRigth", i_strContext);

		return mapItem;
	}

	//将所有item的背景重置
	public void ResetAllItemBak() {

		int iCnt = m_listViewShowInfo.getCount();

		for (int i = 0; i < iCnt; i++) {
			m_listViewShowInfo.getChildAt(i).setBackgroundResource(R.color.white);
		}
	}
}
