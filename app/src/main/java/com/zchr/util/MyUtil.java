/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.zchr.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public final class MyUtil {
	private final static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private MyUtil() {
	}

	public static byte[] toBytes(int a) {
		return new byte[] { (byte) (0x000000ff & (a >>> 24)), (byte) (0x000000ff & (a >>> 16)), (byte) (0x000000ff & (a >>> 8)), (byte) (0x000000ff & (a)) };
	}

	public static int toInt(byte[] b, int s, int n) {
		int ret = 0;

		final int e = s + n;
		for (int i = s; i < e; ++i) {
			ret <<= 8;
			ret |= b[i] & 0xFF;
		}
		return ret;
	}

	public static int toIntR(byte[] b, int s, int n) {
		int ret = 0;

		for (int i = s; (i >= 0 && n > 0); --i, --n) {
			ret <<= 8;
			ret |= b[i] & 0xFF;
		}
		return ret;
	}

	public static int toInt(byte... b) {
		int ret = 0;
		for (final byte a : b) {
			ret <<= 8;
			ret |= a & 0xFF;
		}
		return ret;
	}

	public static String toHexString(byte[] d, int s, int n) {
		final char[] ret = new char[n * 2];
		final int e = s + n;

		int x = 0;
		for (int i = s; i < e; ++i) {
			final byte v = d[i];
			ret[x++] = HEX[0x0F & (v >> 4)];
			ret[x++] = HEX[0x0F & v];
		}
		return new String(ret);
	}

	public static String toHexStringR(byte[] d, int s, int n) {
		final char[] ret = new char[n * 2];

		int x = 0;
		for (int i = s + n - 1; i >= s; --i) {
			final byte v = d[i];
			ret[x++] = HEX[0x0F & (v >> 4)];
			ret[x++] = HEX[0x0F & v];
		}
		return new String(ret);
	}

	public static int parseInt(String txt, int radix, int def) {
		int ret;
		try {
			ret = Integer.valueOf(txt, radix);
		} catch (Exception e) {
			ret = def;
		}

		return ret;
	}

	public static String toAmountString(float value) {
		return String.format("%.2f", value);
	}

	public static String hexBytetoString(byte[] d, int s, int n) {
		String strRslt = "";

		for (int i = 0; i < n; i++) {
			strRslt += String.format("%02X", d[i + s]);
		}

		return strRslt;
	}

	public static int hexBytetoInt(byte[] d, int s, int n) {
		int iRslt = 0;

		String strData = "";

		for (int i = 0; i < n; i++) {
			strData += String.format("%02X", d[i + s]);
		}

		iRslt = Integer.parseInt(strData);

		return iRslt;
	}

	/** Utility class to convert a byte array to a hexadecimal string.
	 * 
	 * @param bytes
	 *            Bytes to convert
	 * @return String, containing hexadecimal representation. */
	public static String ByteArrayToHexString(byte[] bytes) {
		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/** Utility class to convert a byte array to a hexadecimal string.
	 * 
	 * @param bytes
	 *            Bytes to convert
	 * @return String, containing hexadecimal representation. */
	public static String ByteArrayToHexString(byte[] bytes, int iOffSet, int iCnt) {

		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[iCnt * 2];
		int v;
		for (int j = 0; j < iCnt; j++) {
			v = bytes[iOffSet - 1 + j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}

	/** Utility class to convert a byte array to a hexadecimal string.
	 * 
	 * @param bytes
	 *            Bytes to convert
	 * @return String, containing hexadecimal representation. */
	public static String HextoAsc(byte[] bytes, int iOffSet, int iCnt) {

		final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[iCnt * 2];
		int v;
		for (int j = 0; j < iCnt; j++) {
			v = bytes[iOffSet + j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}

	/** Utility class to convert a hexadecimal string to a byte string.
	 * 
	 * <p>
	 * Behavior with input strings containing non-hexadecimal characters is undefined.
	 * 
	 * @param s
	 *            String containing hexadecimal characters to convert
	 * @return Byte array generated from input */
	public static byte[] HexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/** Utility class to convert a hexadecimal string to a byte string.
	 * 
	 * <p>
	 * Behavior with input strings containing non-hexadecimal characters is undefined.
	 * 
	 * @param s
	 *            String containing hexadecimal characters to convert
	 * @return Byte array generated from input */
	public static byte[] AsctoHex(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len - 2; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/** 将32位的int值放到4字节的里
	 * 
	 * @param num
	 * @return */
	public static byte[] int2byteArray(int num) {
		byte[] result = new byte[4];
		result[0] = (byte) (num >>> 24);// 取最高8位放到0下标
		result[1] = (byte) (num >>> 16);// 取次高8为放到1下标
		result[2] = (byte) (num >>> 8); // 取次低8位放到2下标
		result[3] = (byte) (num); // 取最低8位放到3下标
		return result;
	}

	/** 将4字节的byte数组转成一个int值
	 * 
	 * @param b
	 * @return */
	public static int byteArray2int(byte[] b) {
		byte[] a = new byte[4];
		int i = a.length - 1, j = b.length - 1;
		for (; i >= 0; i--, j--) {// 从b的尾部(即int值的低位)开始copy数据
			if (j >= 0)
				a[i] = b[j];
			else
				a[i] = 0;// 如果b.length不足4,则将高位补0
		}
		int v0 = (a[0] & 0xff) << 24;// &0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
		int v1 = (a[1] & 0xff) << 16;
		int v2 = (a[2] & 0xff) << 8;
		int v3 = (a[3] & 0xff);
		return v0 + v1 + v2 + v3;
	}

	/** 转换short为byte
	 * 
	 * @param b
	 * @param s
	 *            需要转换的short
	 * @param index */
	public static void putShort(byte b[], short s, int index) {
		b[index + 1] = (byte) (s >> 8);
		b[index + 0] = (byte) (s >> 0);
	}

	/** 通过byte数组取到short
	 * 
	 * @param b
	 * @param index
	 *            第几位开始取
	 * @return */
	public static short getShort(byte[] b, int index) {
		return (short) (((b[index + 1] << 8) | b[index + 0] & 0xff));
	}

	/** 字符到字节转换
	 * 
	 * @param ch
	 * @return */
	public static void putChar(byte[] bb, char ch, int index) {
		int temp = (int) ch;
		// byte[] b = new byte[2];
		for (int i = 0; i < 2; i++) {
			// 将最高位保存在最低位
			bb[index + i] = new Integer(temp & 0xff).byteValue();
			temp = temp >> 8; // 向右移8位
		}
	}

	/** 字节到字符转换
	 * 
	 * @param b
	 * @return */
	public static char getChar(byte[] b, int index) {
		int s = 0;
		if (b[index + 1] > 0)
			s += b[index + 1];
		else
			s += 256 + b[index + 0];
		s *= 256;
		if (b[index + 0] > 0)
			s += b[index + 1];
		else
			s += 256 + b[index + 0];
		char ch = (char) s;
		return ch;
	}

	/** float转换byte
	 * 
	 * @param bb
	 * @param x
	 * @param index */
	public static void putFloat(byte[] bb, float x, int index) {
		// byte[] b = new byte[4];
		int l = Float.floatToIntBits(x);
		for (int i = 0; i < 4; i++) {
			bb[index + i] = new Integer(l).byteValue();
			l = l >> 8;
		}
	}

	/** 通过byte数组取得float
	 * 
	 * @param bb
	 * @param index
	 * @return */
	public static float getFloat(byte[] b, int index) {
		int l;
		l = b[index + 0];
		l &= 0xff;
		l |= ((long) b[index + 1] << 8);
		l &= 0xffff;
		l |= ((long) b[index + 2] << 16);
		l &= 0xffffff;
		l |= ((long) b[index + 3] << 24);
		return Float.intBitsToFloat(l);
	}

	/** double转换byte
	 * 
	 * @param bb
	 * @param x
	 * @param index */
	public static void putDouble(byte[] bb, double x, int index) {
		// byte[] b = new byte[8];
		long l = Double.doubleToLongBits(x);
		for (int i = 0; i < 4; i++) {
			bb[index + i] = new Long(l).byteValue();
			l = l >> 8;
		}
	}

	/** 通过byte数组取得float
	 * 
	 * @param bb
	 * @param index
	 * @return */
	public static double getDouble(byte[] b, int index) {
		long l;
		l = b[0];
		l &= 0xff;
		l |= ((long) b[1] << 8);
		l &= 0xffff;
		l |= ((long) b[2] << 16);
		l &= 0xffffff;
		l |= ((long) b[3] << 24);
		l &= 0xffffffffl;
		l |= ((long) b[4] << 32);
		l &= 0xffffffffffl;
		l |= ((long) b[5] << 40);
		l &= 0xffffffffffffl;
		l |= ((long) b[6] << 48);
		l &= 0xffffffffffffffl;
		l |= ((long) b[7] << 56);
		return Double.longBitsToDouble(l);
	}

	public static short makeShort(byte[] byteNum, int iOffSet) {
		return (short) ((short) ((byteNum[iOffSet] & 0xFF) << 8) + (short) (byteNum[iOffSet + 1] & 0xFF));
	}

	/** 設置View的寬度（像素），若設置爲自適應則應該傳入MarginLayoutParams.WRAP_CONTENT
	 * 
	 * @param view
	 * @param width */
	public static void setLayoutWidth(View view, int width, int height) {
		/*
		 * MarginLayoutParams margin=new
		 * MarginLayoutParams(view.getLayoutParams());
		 * margin.setMargins(margin.leftMargin,y, margin.rightMargin,
		 * y+margin.height); //RelativeLayout.LayoutParams layoutParams = new
		 * RelativeLayout.LayoutParams(margin);
		 * //view.setLayoutParams(layoutParams); ViewGroup.MarginLayoutParams
		 * layoutParams =newLayParms(view, margin);
		 * //RelativeLayout.LayoutParams layoutParams = new
		 * RelativeLayout.LayoutParams(margin);
		 * view.setLayoutParams(layoutParams); view.requestLayout();
		 */
		if (view.getParent() instanceof FrameLayout) {
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
			lp.width = width;
			lp.height = height;
			view.setLayoutParams(lp);
			// view.setX(x);
			view.requestLayout();
		} else if (view.getParent() instanceof RelativeLayout) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
			lp.width = width;
			lp.height = height;
			view.setLayoutParams(lp);
			// view.setX(x);
			view.requestLayout();
		} else if (view.getParent() instanceof LinearLayout) {
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
			lp.width = width;
			lp.height = height;
			view.setLayoutParams(lp);
			// view.setX(x);
			view.requestLayout();
		}
	}

	/** 按地图的缩放等级 转换出地图应该加载的范围
	 * 
	 * @param level
	 * @return */
	public static float CalcMapAround2(float level) {
		level -= 3;

		if (level <= 3) {
			return 2f;
		} else if (level <= 4) {
			return 1f;
		} else if (level <= 5) {
			return 0.5f;
		} else if (level <= 6) {
			return 0.2f;
		} else if (level <= 7) {
			return 0.1f;
		} else if (level <= 8) {
			return 0.05f;
		} else if (level <= 9) {
			return 0.025f;
		} else if (level <= 10) {
			return 0.02f;
		} else if (level <= 11) {
			return 0.01f;
		} else if (level <= 12) {
			return 0.005f;
		} else if (level <= 13) {
			return 0.002f;
		} else if (level <= 14) {
			return 0.001f;
		} else if (level <= 15) {
			return 0.0005f;
		} else if (level <= 16) {
			return 0.0002f;
		} else if (level <= 17) {
			return 0.0001f;
		} else {
			return 0.00005f;
		}

	}

	public static int GetSysConfig(Activity i_activity) {

		SysConfig sysConfig = null;

		SharedPreferences sharedPreferences = i_activity.getSharedPreferences("SysConfig", Activity.MODE_PRIVATE);

		try {

			String strServerIp = sharedPreferences.getString("ServerIp", "");
			String strServerPort = sharedPreferences.getString("ServerPort", "");
			String strIccInterface = sharedPreferences.getString("IccInterface", "");
			String strNetTimeOut = sharedPreferences.getString("NetTimeOut", "");
			String strReadCardTimeOut = sharedPreferences.getString("ReadCardTimeOut", "");

			sysConfig = new SysConfig();

			if (!strServerIp.isEmpty()) {
				DefineFinal.setServerIp(strServerIp);
			} else {
				DefineFinal.setServerIp("192.168.1.1");
			}

			if (!strServerPort.isEmpty()) {
				DefineFinal.setServerPort(Integer.parseInt(strServerPort));
			} else {
				DefineFinal.setServerPort(6802);
			}

			if (!strNetTimeOut.isEmpty()) {
				DefineFinal.setNetTimeOut(Integer.parseInt(strNetTimeOut));
			} else {
				DefineFinal.setNetTimeOut(30000);
			}

			if (!strReadCardTimeOut.isEmpty()) {
				DefineFinal.setReadCardTimeOut(Integer.parseInt(strReadCardTimeOut));
			} else {
				DefineFinal.setNetTimeOut(30000);
			}

			DefineFinal.setIccInterface(Integer.parseInt(strIccInterface));

		} catch (Exception e) {
			return 1;
		}

		return 0;
	}
}
