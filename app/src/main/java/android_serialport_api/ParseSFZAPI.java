package android_serialport_api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.authentication.activity.R;
import com.authentication.utils.DataUtils;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.ivsign.android.IDCReader.IDCReaderSDK;
import com.ivsign.android.IDCReader.SfzFileManager;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

public class ParseSFZAPI {
	private String src=Environment.getExternalStorageDirectory().getAbsolutePath()+"/sfzPic"; 
	private static final byte[] command1 = "D&C00040101".getBytes();
	private static final byte[] command2 = "D&C00040102".getBytes();
	private static final byte[] command3 = "D&C00040108".getBytes();
	private static final byte[] SFZ_ID_COMMAND1 = "f050000\r\n".getBytes();
	private static final byte[] SFZ_ID_COMMAND2 = "f1d0000000000080108\r\n"
			.getBytes();
	private static final byte[] SFZ_ID_COMMAND3 = "f0036000008\r\n".getBytes();
	private static final String TURN_ON = "c050601\r\n";// 打开天线厂
	private static final String TURN_OFF = "c050602\r\n";// 关闭天线厂

	private static final String SFZ_ID_RESPONSE1 = "5000000000";
	private static final String SFZ_ID_RESPONSE2 = "08";
	private static final String TURN_OFF_RESPONSE = "RF carrier off!";

	private static final String CARD_SUCCESS = "AAAAAA96690508000090";
	private static final String CARD_SUCCESS2 = "AAAAAA9669090A000090";

	private static final String TIMEOUT_RETURN = "AAAAAA96690005050505";
	private static final String CMD_ERROR = "CMD_ERROR";

	private static final String MODULE_SUCCESS = "AAAAAA96690014000090";

	public static final int DATA_SIZE = 2321;

	private byte[] buffer = new byte[DATA_SIZE];

	private String path;

	public static final int SECOND_GENERATION_CARD = 1295;

	public static final int THIRD_GENERATION_CARD = 2321;

	private Context m_Context;

	public ParseSFZAPI(Looper looper, String rootPath, Context context) {
		this.path = rootPath + File.separator + "wltlib";
		this.m_Context = context;
	}

	private Result result;

	private final Logger logger = LoggerFactory.getLogger();

	/**
	 * 读取身份证信息，此方法为阻塞的，建议放在子线程处理
	 * 
	 * @return true：成功获取身份证信息，false：返回数据出错，可能是超时，读卡出错，寻卡失败等。
	 */
	public Result read(int cardType) {
		People people = null;
		if (cardType == SECOND_GENERATION_CARD) {
			SerialPortManager.getInstance().write(command1);
		} else if (cardType == THIRD_GENERATION_CARD) {
			SerialPortManager.getInstance().write(command3);
		} else {
			return null;
		}
		int length = 0;
		result = new Result();
		SerialPortManager.switchRFID = false;
		length = SerialPortManager.getInstance().read(buffer, 3000, 100);

		long time = System.currentTimeMillis();
		while (length == 0 && (System.currentTimeMillis() - time) <= 4000 && SerialPortManager.getInstance().isOpen()) {
			// add by yjj at 2017/1/13 11:00
			try {
				SerialPortManager.getInstance().closeSerialPort();
				Thread.sleep(1500);
				Log.d("jokey", "SFZ:" + SerialPortManager.getInstance().isOpen());
				SerialPortManager.getInstance().openSerialPort();
				Thread.sleep(1500);
				Log.d("jokey", "SFZ:" + SerialPortManager.getInstance().isOpen());
				if (cardType == SECOND_GENERATION_CARD) {
					SerialPortManager.getInstance().write(command1);
				} else if (cardType == THIRD_GENERATION_CARD) {
					SerialPortManager.getInstance().write(command3);
				} else {
					return null;
				}
				length = SerialPortManager.getInstance().read(buffer, 3000, 100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// end
		}

		if (length == 0) {
			result.confirmationCode = Result.TIME_OUT;
			return result;
		}

		if (length == 1297 && cardType == THIRD_GENERATION_CARD) {
			result.confirmationCode = Result.NO_THREECARD;
			return result;
		}

		people = decode(buffer, length);
		if (people == null) {
			result.confirmationCode = Result.FIND_FAIL;
		} else {
			result.confirmationCode = Result.SUCCESS;
			result.resultInfo = people;
		}
		return result;
	}

	public Result readModule() {
		result = new Result();
		SerialPortManager.switchRFID = false;
		SerialPortManager.getInstance().write(command2);
		byte[] buffer = new byte[DATA_SIZE];
		int length = SerialPortManager.getInstance().read(buffer, 3000, 300);
		if (length == 0) {
			result.confirmationCode = Result.TIME_OUT;
			return result;
		}
		byte[] module = new byte[length];
		System.arraycopy(buffer, 0, module, 0, length);
		String data = DataUtils.toHexString(module);
		if (length > 10) {
			String prefix = data.substring(0, 20);
			if (prefix.equalsIgnoreCase(MODULE_SUCCESS)) {
				String temp1 = DataUtils.toHexString1(module[10]);
				String temp2 = DataUtils.toHexString1(module[12]);
				byte[] temp3 = new byte[4];
				System.arraycopy(module, 14, temp3, 0, temp3.length);
				reversal(temp3);
				byte[] temp4 = new byte[4];
				System.arraycopy(module, 18, temp4, 0, temp4.length);
				reversal(temp4);
				byte[] temp5 = new byte[4];
				System.arraycopy(module, 22, temp5, 0, temp5.length);
				reversal(temp5);
				StringBuffer sb = new StringBuffer();
				sb.append(temp1);
				sb.append(".");
				sb.append(temp2);
				sb.append("-");
				sb.append(byte2Int(temp3));
				sb.append("-");
				String str4 = Long.toString(byte2Int(temp4));
				for (int i = 0; i < 10 - str4.length(); i++) {
					sb.append("0");
				}
				sb.append(str4);
				sb.append("-");
				String str5 = Long.toString(byte2Int(temp5));
				for (int i = 0; i < 10 - str5.length(); i++) {
					sb.append("0");
				}
				sb.append(str5);
				result.confirmationCode = Result.SUCCESS;
				result.resultInfo = sb.toString();
				return result;
			}
		}
		result.confirmationCode = Result.FIND_FAIL;
		return result;
	}

	public String readCardID() {
		if (!SerialPortManager.switchRFID) {
			SerialPortManager.getInstance().switchStatus();
		}
		turnOff();
		Log.i("whw", "readCardID");
		if (sendReceive(SFZ_ID_COMMAND1, SFZ_ID_RESPONSE1)) {
			if (sendReceive(SFZ_ID_COMMAND2, SFZ_ID_RESPONSE2)) {
				return sendReceive(SFZ_ID_COMMAND3);
			}
		}
		return "";
	}

	private boolean sendReceive(byte[] command, String response) {
		SerialPortManager.getInstance().write(command);
		int length = SerialPortManager.getInstance().read(buffer, 3000, 10);
		if (length > 0) {
			String dataStr = new String(buffer, 0, length).trim();
			Log.i("whw", "dataStr=" + dataStr);
			if (dataStr.startsWith(response)) {
				return true;
			}
		}
		return false;
	}

	// 关闭天线厂
	public boolean turnOff() {
		byte[] command = TURN_OFF.getBytes();
		SerialPortManager.getInstance().write(command);
		int length = SerialPortManager.getInstance().read(buffer, 3000, 10);
		String str = "";
		if (length > 0) {
			str = new String(buffer, 0, length).trim();
			if (str.equals(TURN_OFF_RESPONSE)) {
				return true;
			}
		}
		return false;
	}

	public boolean turnOn() {
		byte[] command = TURN_ON.getBytes();
		SerialPortManager.getInstance().write(command);
		int length = SerialPortManager.getInstance().read(buffer, 3000, 10);
		String str = "";
		if (length > 0) {
			str = new String(buffer, 0, length).trim();
			if (str.equals(TURN_OFF_RESPONSE)) {
				return true;
			}
		}
		return false;
	}

	private String sendReceive(byte[] command) {
		SerialPortManager.getInstance().write(command);
		int length = SerialPortManager.getInstance().read(buffer, 3000, 10);
		if (length > 0) {
			String dataStr = new String(buffer, 0, length).trim();
			Log.i("whw", "dataStr=" + dataStr);
			if (dataStr.endsWith("9000"))
				return dataStr.substring(0, 16);
		}
		return "";
	}

	private void reversal(byte[] data) {
		int length = data.length;
		for (int i = 0; i < length / 2; i++) {
			byte temp = data[i];
			data[i] = data[length - 1 - i];
			data[length - 1 - i] = temp;
		}
	}

	private long byte2Int(byte[] data) {
		int intValue = 0;
		for (int i = 0; i < data.length; i++) {
			intValue += (data[i] & 0xff) << (8 * (3 - i));
		}
		long temp = intValue;
		temp <<= 32;
		temp >>>= 32;
		return temp;
	}

	private People decode(byte[] buffer, int length) {
		if (buffer == null) {
			return null;
		}
		byte[] b = new byte[10];
		System.arraycopy(buffer, 0, b, 0, 10);
		String result = toHexString(b);
		Log.i("whw", "result sfz=" + result);
		People people = null;
		if (result.equalsIgnoreCase(CARD_SUCCESS)
				|| result.equalsIgnoreCase(CARD_SUCCESS2)) {
			byte[] data = new byte[buffer.length - 10];
			System.arraycopy(buffer, 10, data, 0, buffer.length - 10);
			people = decodeInfo(data, length);
		} else if (result.equalsIgnoreCase(TIMEOUT_RETURN)) {
			logger.debug(TIMEOUT_RETURN);
		} else if (result.startsWith(CMD_ERROR)) {
			logger.debug(CMD_ERROR);
		}
		return people;

	}

	private People decodeInfo(byte[] buffer, int length) {
		short textSize = getShort(buffer[0], buffer[1]);
		short imageSize = getShort(buffer[2], buffer[3]);
		short modelSize = 0;
		byte[] model = null;
		short skipLength = 0;
		if (length == THIRD_GENERATION_CARD) {
			modelSize = getShort(buffer[4], buffer[5]);
			skipLength = 2;
			model = new byte[modelSize];
			System.arraycopy(buffer, 4 + skipLength + textSize + imageSize,
					model, 0, modelSize);
		}
		byte[] text = new byte[textSize];
		System.arraycopy(buffer, 4 + skipLength, text, 0, textSize);
		byte[] image = new byte[imageSize];
		System.arraycopy(buffer, 4 + skipLength + textSize, image, 0, imageSize);

		People people = null;
		try {
			String temp = null;
			people = new People();
			people.setHeadImage(image);
			// 姓名
			temp = new String(text, 0, 30, "UTF-16LE").trim();
			people.setPeopleName(temp);

			// 性别
			temp = new String(text, 30, 2, "UTF-16LE");
			if (temp.equals("1"))
				temp = "男";
			else
				temp = "女";
			people.setPeopleSex(temp);

			// 民族
			temp = new String(text, 32, 4, "UTF-16LE");
			try {
				int code = Integer.parseInt(temp.toString());
				temp = decodeNation(code);
			} catch (Exception e) {
				temp = "";
			}
			people.setPeopleNation(temp);

			// 出生
			temp = new String(text, 36, 16, "UTF-16LE").trim();
			people.setPeopleBirthday(temp);

			// 住址
			temp = new String(text, 52, 70, "UTF-16LE").trim();
			people.setPeopleAddress(temp);

			// 身份证号
			temp = new String(text, 122, 36, "UTF-16LE").trim();
			people.setPeopleIDCode(temp);

			// 签发机关
			temp = new String(text, 158, 30, "UTF-16LE").trim();
			people.setDepartment(temp);

			// 有效起始日期
			temp = new String(text, 188, 16, "UTF-16LE").trim();
			people.setStartDate(temp);

			// 有效截止日期
			temp = new String(text, 204, 16, "UTF-16LE").trim();
			people.setEndDate(temp);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}

		people.setPhoto(parsePhoto(image));
		people.setModel(model);
		return people;
	}

	private String decodeNation(int code) {
		String nation;
		switch (code) {
		case 1:
			nation = "汉";
			break;
		case 2:
			nation = "蒙古";
			break;
		case 3:
			nation = "回";
			break;
		case 4:
			nation = "藏";
			break;
		case 5:
			nation = "维吾尔";
			break;
		case 6:
			nation = "苗";
			break;
		case 7:
			nation = "彝";
			break;
		case 8:
			nation = "壮";
			break;
		case 9:
			nation = "布依";
			break;
		case 10:
			nation = "朝鲜";
			break;
		case 11:
			nation = "满";
			break;
		case 12:
			nation = "侗";
			break;
		case 13:
			nation = "瑶";
			break;
		case 14:
			nation = "白";
			break;
		case 15:
			nation = "土家";
			break;
		case 16:
			nation = "哈尼";
			break;
		case 17:
			nation = "哈萨克";
			break;
		case 18:
			nation = "傣";
			break;
		case 19:
			nation = "黎";
			break;
		case 20:
			nation = "傈僳";
			break;
		case 21:
			nation = "佤";
			break;
		case 22:
			nation = "畲";
			break;
		case 23:
			nation = "高山";
			break;
		case 24:
			nation = "拉祜";
			break;
		case 25:
			nation = "水";
			break;
		case 26:
			nation = "东乡";
			break;
		case 27:
			nation = "纳西";
			break;
		case 28:
			nation = "景颇";
			break;
		case 29:
			nation = "柯尔克孜";
			break;
		case 30:
			nation = "土";
			break;
		case 31:
			nation = "达斡尔";
			break;
		case 32:
			nation = "仫佬";
			break;
		case 33:
			nation = "羌";
			break;
		case 34:
			nation = "布朗";
			break;
		case 35:
			nation = "撒拉";
			break;
		case 36:
			nation = "毛南";
			break;
		case 37:
			nation = "仡佬";
			break;
		case 38:
			nation = "锡伯";
			break;
		case 39:
			nation = "阿昌";
			break;
		case 40:
			nation = "普米";
			break;
		case 41:
			nation = "塔吉克";
			break;
		case 42:
			nation = "怒";
			break;
		case 43:
			nation = "乌孜别克";
			break;
		case 44:
			nation = "俄罗斯";
			break;
		case 45:
			nation = "鄂温克";
			break;
		case 46:
			nation = "德昂";
			break;
		case 47:
			nation = "保安";
			break;
		case 48:
			nation = "裕固";
			break;
		case 49:
			nation = "京";
			break;
		case 50:
			nation = "塔塔尔";
			break;
		case 51:
			nation = "独龙";
			break;
		case 52:
			nation = "鄂伦春";
			break;
		case 53:
			nation = "赫哲";
			break;
		case 54:
			nation = "门巴";
			break;
		case 55:
			nation = "珞巴";
			break;
		case 56:
			nation = "基诺";
			break;
		case 97:
			nation = "其他";
			break;
		case 98:
			nation = "外国血统中国籍人士";
			break;
		default:
			nation = "";
		}

		return nation;
	}

	/**
	 * 数组转成16进制字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String toHexString(byte[] b) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			buffer.append(toHexString1(b[i]));
		}
		return buffer.toString();
	}

	public static String toHexString1(byte b) {
		String s = Integer.toHexString(b & 0xFF);
		if (s.length() == 1) {
			return "0" + s;
		} else {
			return s;
		}
	}

	private short getShort(byte b1, byte b2) {
		short temp = 0;
		temp |= (b1 & 0xff);
		temp <<= 8;
		temp |= (b2 & 0xff);
		return temp;
	}

	private byte[] parsePhoto(byte[] wltdata) {
		SfzFileManager sfzFileManager = new SfzFileManager(src);
		if (sfzFileManager.initDB(this.m_Context, R.raw.base, R.raw.license)) {
			int ret = IDCReaderSDK.Init();
			if (0 == ret) {
				ret = IDCReaderSDK.unpack(buffer);
				if (1 == ret) {
					byte[] image = IDCReaderSDK.getPhoto();
					return image;
				}
			}
		}
		return null;
	}

	private boolean isExistsParsePath(String wltPath, byte[] wltdata) {
		File myFile = new File(path);
		boolean isMKDir = true;
		if (!myFile.exists()) {
			isMKDir = myFile.mkdir();
		}
		if (!isMKDir) {
			return false;
		}

		File wltFile = new File(wltPath);
		boolean isCreate = true;
		if (!wltFile.exists()) {
			try {
				isCreate = wltFile.createNewFile();
			} catch (IOException e) {
				isCreate = false;
				e.printStackTrace();
			}
		}
		if (!isCreate) {
			return false;
		}

		boolean isWriteData = false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(wltFile);
			fos.write(wltdata);
			fos.flush();
			isWriteData = true;
		} catch (FileNotFoundException e) {
			isWriteData = false;
			e.printStackTrace();
		} catch (IOException e) {
			isWriteData = false;
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isWriteData;
	}

	/**
	 * 获得指定文件的byte数组
	 */
	public static byte[] getBytes(String filePath) {
		byte[] buffer = null;
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	public static class People {
		/**
		 * 姓名
		 */
		private String peopleName;

		/**
		 * 性别
		 */
		private String peopleSex;

		/**
		 * 民族
		 */
		private String peopleNation;

		/**
		 * 出生日期
		 */
		private String peopleBirthday;

		/**
		 * 住址
		 */
		private String peopleAddress;

		/**
		 * 身份证号
		 */
		private String peopleIDCode;

		/**
		 * 签发机关
		 */
		private String department;

		/**
		 * 有效期限：开始
		 */
		private String startDate;

		/**
		 * 有效期限：结束
		 */
		private String endDate;

		/**
		 * 身份证头像
		 */
		private byte[] photo;

		/**
		 * 没有解析成图片的数据大小一般为1024字节
		 */
		private byte[] headImage;

		/**
		 * 三代证指纹模板数据，正常位1024，如果为null，说明为二代证，没有指纹模板数据
		 */
		private byte[] model;

		public String getPeopleName() {
			return peopleName;
		}

		public void setPeopleName(String peopleName) {
			this.peopleName = peopleName;
		}

		public String getPeopleSex() {
			return peopleSex;
		}

		public void setPeopleSex(String peopleSex) {
			this.peopleSex = peopleSex;
		}

		public String getPeopleNation() {
			return peopleNation;
		}

		public void setPeopleNation(String peopleNation) {
			this.peopleNation = peopleNation;
		}

		public String getPeopleBirthday() {
			return peopleBirthday;
		}

		public void setPeopleBirthday(String peopleBirthday) {
			this.peopleBirthday = peopleBirthday;
		}

		public String getPeopleAddress() {
			return peopleAddress;
		}

		public void setPeopleAddress(String peopleAddress) {
			this.peopleAddress = peopleAddress;
		}

		public String getPeopleIDCode() {
			return peopleIDCode;
		}

		public void setPeopleIDCode(String peopleIDCode) {
			this.peopleIDCode = peopleIDCode;
		}

		public String getDepartment() {
			return department;
		}

		public void setDepartment(String department) {
			this.department = department;
		}

		public String getStartDate() {
			return startDate;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public String getEndDate() {
			return endDate;
		}

		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}

		public byte[] getPhoto() {
			return photo;
		}

		public void setPhoto(byte[] photo) {
			this.photo = photo;
		}

		public byte[] getHeadImage() {
			return headImage;
		}

		public void setHeadImage(byte[] headImage) {
			this.headImage = headImage;
		}

		public byte[] getModel() {
			return model;
		}

		public void setModel(byte[] model) {
			this.model = model;
		}
	}

	public static class Result {
		public static final int SUCCESS = 1;
		public static final int FIND_FAIL = 2;
		public static final int TIME_OUT = 3;
		public static final int OTHER_EXCEPTION = 4;
		public static final int NO_THREECARD = 5;

		/**
		 * 确认码 1: 成功 2：失败 3: 超时 4：其它异常5:不是三代证
		 */
		public int confirmationCode;

		/**
		 * 结果集:当确认码为1时，再判断是否有结果
		 */
		public Object resultInfo;
	}
}