package com.zchr.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Parcelable;

public abstract class NfcReaderBaseActivity extends Activity {

	//成功
	public static final int NFC_BASE_ACTIVITY_SUCCESS = 0x00;

	//失败
	public static final int NFC_BASE_ACTIVITY_ERROR = 0x01;

	//设备不支持
	public static final int NFC_BASE_ACTIVITY_DEV_NO_SUPPORT = 0x02;

	//设备未开启
	public static final int NFC_BASE_ACTIVITY_DEV_NO_ENABLE = 0x03;

	//NFC适配器
	public NfcAdapter m_nfcAdapter;

	//填补Intent
	public PendingIntent m_pendingIntent;

	//状态
	public int m_iState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//application = (MyApplication) this.getApplicationContext();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (m_nfcAdapter != null) {
			m_nfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (m_nfcAdapter != null) {

			CheckDevice();
			m_nfcAdapter.enableForegroundDispatch(this, m_pendingIntent, NfcReader.m_aItentFilter, NfcReader.m_astrTechlists);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (m_nfcAdapter != null) {
			//Android NFC接口
			final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

			NfcReader.SetDep(IsoDep.get((Tag) p));

			//NFC读写处理
			NfcReadWriteProc();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (m_nfcAdapter != null) {

			//检测设备是否支持NFC并开启
			CheckDevice();
		}
	}

	//NFC读写处理
	public abstract void NfcReadWriteProc();

	//初始化NFC接口
	public int InitNfcInterface(Context i_iContext) {

		m_nfcAdapter = NfcAdapter.getDefaultAdapter(i_iContext);
		m_pendingIntent = PendingIntent.getActivity(i_iContext, 0, new Intent(i_iContext, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		//检测设备是否支持NFC并开启
		int iRet = CheckDevice();
		if (iRet != NFC_BASE_ACTIVITY_SUCCESS) {
			return iRet;
		}

		return NFC_BASE_ACTIVITY_SUCCESS;
	}

	//检测设备是否支持NFC并开启
	public int CheckDevice() {

		String StrMsg = "";

		try {
			if (m_nfcAdapter == null) {

				StrMsg = "设备不支持NFC";
				m_iState = NFC_BASE_ACTIVITY_DEV_NO_SUPPORT;
			} else if (!m_nfcAdapter.isEnabled()) {

				StrMsg = "NFC设备未开启";
				m_iState = NFC_BASE_ACTIVITY_DEV_NO_ENABLE;
			} else {

				StrMsg = "成功";
				m_iState = NFC_BASE_ACTIVITY_SUCCESS;
				return NFC_BASE_ACTIVITY_SUCCESS;
			}
		} catch (Exception e) {

			StrMsg = "NFC设备检测异常,请重新开启";
			m_iState = NFC_BASE_ACTIVITY_ERROR;
		}

		Dialog dialog = new AlertDialog.Builder(this).setMessage(StrMsg).create();
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		dialog.show();

		return m_iState;
	}
}
