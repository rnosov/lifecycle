package com.zchr.jni.tonative;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;
import android.widget.Toast;

import com.example.iccardapi.IcInterface;
import com.zchr.nfc.ISO7816;
import com.zchr.nfc.NfcReader;
import com.zchr.rd.tcpclient.RspData;
import com.zchr.util.DebugLog;
import com.zchr.util.DefineFinal;
import com.zchr.util.ReturnIntData;

public class IccInterface {

	//Ic卡通讯初始化
	static public int IccInit(ReturnIntData io_sRetIntData) {

		io_sRetIntData.setIntData(DefineFinal.iIccInterface);

		if (DefineFinal.getIccInterface() == 1 || DefineFinal.getIccInterface() == 2) {

			
			IcInterface.setGpioFlg(true);
			if (!IcInterface.isOpen()) {
				if (!IcInterface.openSerialPort()) {
					return 1;
				}
			}
		}

		Date dateStart = new SimpleDateFormat("yyyyMMddHHmmssSSS").get2DigitYearStart();

		while (true) {

			if (DefineFinal.iIccInterface == 0) {

				//Android Nfc
				if (NfcReader.ConnectCard() == NfcReader.CARD_READER_SUCCESS) {
					break;
				}
			} else if (DefineFinal.iIccInterface == 1) {
				Log.i("CY", "IcInterface--->resetCard" );
				
				if (IcInterface.resetCard(0)) {
					break;
				}
			} else if (DefineFinal.iIccInterface == 2) {

				if (IcInterface.resetCard(1)) {
					break;
				}
			}

			// 是否超时
			Date dateEnd = new SimpleDateFormat("yyyyMMddHHmmssSSS").get2DigitYearStart();
			long timeDelta = (dateEnd.getTime() - dateStart.getTime());
			if (timeDelta > DefineFinal.getReadCardTimeOut()) {
				return 2;
			}
		}

		return 0;
	}

	//APDU通讯
	static int Apdu(byte[] i_abyteCmd, int i_iCmdLen, RspData io_sRspData) {

		if (DefineFinal.iIccInterface == 0) {

			//Android Nfc
			ISO7816 rspIso7816 = new ISO7816();
			if (NfcReader.SendApdu(i_abyteCmd, i_iCmdLen, rspIso7816) != NfcReader.CARD_READER_SUCCESS) {
				return 1;
			}

			byte[] byteRsp = new byte[rspIso7816.getiRspDataLen() + 2];

			if (rspIso7816.getiRspDataLen() > 0) {
				System.arraycopy(rspIso7816.getByteRspData(), 0, byteRsp, 0, rspIso7816.getiRspDataLen());
			}

			System.arraycopy(rspIso7816.getSW(), 0, byteRsp, rspIso7816.getiRspDataLen(), 2);

			io_sRspData.setRspData(byteRsp);
			io_sRspData.setRspDataLen(rspIso7816.getiRspDataLen() + 2);
		} else if (DefineFinal.iIccInterface == 1 || DefineFinal.iIccInterface == 2) {

			byte[] byteRsp = new byte[255];

			int iRspLen = IcInterface.IcExchange(i_abyteCmd, i_iCmdLen, byteRsp, byteRsp.length);
			Log.d("jokey", "Apdu---->iRspLen: "+iRspLen);
			if (iRspLen <= 0) {
				return 1;
			}

			io_sRspData.setRspData(byteRsp);
			io_sRspData.setRspDataLen(iRspLen);
		}

		return 0;
	}

	//关闭Ic卡通讯
	public static int IccClose() {

		if (DefineFinal.iIccInterface == 0) {

			//Android Nfc
			NfcReader.ClostConnect();
		} else if (DefineFinal.iIccInterface == 1 || DefineFinal.iIccInterface == 2) {
			//IcInterface.closeSerialPort();
		}

		return 0;
	}
}
