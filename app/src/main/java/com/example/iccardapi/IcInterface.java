package com.example.iccardapi;

import android.os.SystemClock;
import android.util.Log;
import android_serialport_api.SerialPort;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.authentication.utils.DataUtils;

public class IcInterface
{
  private static final String m_Tag = "CY";
  private static int m_Baudrate = 460800;//
  private static String m_Path = "/dev/ttyHSL0";//
  private static final byte[] m_PowerOn = { '1' };//
  private static final byte[] m_PowerDown = { '0' };//
  private static String m_GpioDev = "/sys/class/pwv_gpios/as602-en/enable";
  private static boolean m_GpioFlg = true;

  private static SerialPort m_SerialPort = null;

  private static boolean m_FirstOpen = false;
  private static boolean m_IsOpen;
  private static InputStream m_InputStream;
  private static OutputStream m_OutputStream;
  private static ReadThread m_ReadThread;
  private static byte[] m_Buffer = new byte[51200];
  private static int m_CurrentSize = 0;
  private static byte[] previous;
  public IcInterface()
  {
    Log.i("CY", "Enter function IcInterface().");
  }

  public static boolean resetCard(int cardType)
  {
    Log.i("CY", "Enter function resetCard().");

    byte[] recvData = new byte[1024];

    if (cardType == 0)
    {
//      byte[] cmdReset = new byte[4];
//      write(cmdReset);
      Log.i("jokey", "write--->CMD_OPEN_SWITCH" );

    }
    else if (1 == cardType)
    {
      byte[] cmdReset = { 1, 0, 0, 1 };
      write(cmdReset);
    }
    else
    {
      return false;
    }

    int length = read(recvData, 150, 10);
    if (length == 0)
    {
      return false;
    }

    byte[] retData = new byte[length];
    System.arraycopy(recvData, 0, retData, 0, length);
    Log.i("CY", "After reset, get data:" + DataUtils.toHexString(retData));

    if ((3 == recvData[0]) && (1 == recvData[1]) && (1 == recvData[2]) && (3 == recvData[3]))
    {
      Log.i("CY", "Reset ic card unsuccessfully!!!");
      return false;
    }

    Log.i("CY", "Reset ic card successfully!!!");
    return true;
  }

  public static int IcExchange(byte[] cApdu, int cLen, byte[] rApdu, int rMax)
  {
    Log.i("CY", "Enter function IcExchange().");
    //record previous cmd;
    previous = cApdu;
//    Log.i("jokey", "IcExchange--->cApdu: " + DataUtils.toHexString(cApdu) + ".");
    byte[] cmd = makeProcData(cApdu);
//    Log.i("jokey", "makeProcData--->cmd: " + DataUtils.toHexString(cmd) + ".");
    write(cmd);

    byte[] recvData = new byte[512];
    int length = read(recvData, 500, 10);
    Log.i("jokey", "The return data of apdu is:" + DataUtils.toHexString(recvData) + ".");
    Log.d("jokey", "IcExchange---recvData--->length: "+length);
    
    if(length == 8 ){
    	if(recvData[4]==0x02 ){
    		switch (recvData[5]) {
			case 0x61:
				byte[] resp = getRespose(recvData[6]);
		    	Log.i("jokey", "resp:" + DataUtils.toHexString(resp) + ".");
		    	write(resp);
				break;
			case (byte)0x6c:
				Log.i("jokey", "previous:" + DataUtils.toHexString(previous) + ".");
				previous[4] = recvData[6];
				write(makeProcData(previous));
				break;
			}
	    	byte[] receive = new byte[512];
	        int len = read(receive, 500, 10);
	        Log.i("jokey", "receive:" + DataUtils.toHexString(receive) + ".");
	        Log.d("jokey", "IcExchange---receive--->len: "+len);
	        len -= 6;
	        System.arraycopy(receive, 5, rApdu, 0, len);
	        Log.i("jokey", "rApdu:" + DataUtils.toHexString(rApdu) + ".");
	        previous = cApdu;
	        return len;
    	}
    }else if(length>8){
    	length -= 6;
        System.arraycopy(recvData, 5, rApdu, 0, length);
        Log.i("jokey", "rApdu:" + DataUtils.toHexString(rApdu) + ".");
    }
   
    return length;
  }

  public static boolean openSerialPort()
  {
    Log.i("CY", "Enter function openSerialPort().");

    Log.i("CY", "The baudrate is: " + m_Baudrate +  " ,the serialPort path is: " + getComPath() +  " ,the gpio path is: " + getGpioDev() + "!");

    if (m_SerialPort == null) {
      try{
        if (("" == m_Path) || ("" == m_GpioDev)){
          return false;
        }

        if (m_GpioFlg){
          setUpGpio();
          Log.i("CY", "Gpio status = " + getGpioStatus());
          m_FirstOpen = true;
        }
        m_SerialPort = new SerialPort(new File(m_Path), m_Baudrate, 0);
      }catch (IOException e){
        Log.i("CY", "Open serial port abnormally!!!");
        e.printStackTrace();
        return false;
      }

      m_IsOpen = true;
      m_InputStream = m_SerialPort.getInputStream();
      m_OutputStream = m_SerialPort.getOutputStream();

      m_ReadThread = new ReadThread();
      m_ReadThread.start();

      Log.i("CY", "Open serial port successfully!!!");
      return true;
    }

    Log.i("CY", "Open serial port unsuccessfully!!!");
    return false;
  }

  public static void closeSerialPort()
  {
    Log.i("CY", "Enter function closeSerialPort().");

    if (m_ReadThread != null)
    {
      m_ReadThread.interrupt();
      m_ReadThread = null;
      m_IsOpen = false;
    }

    if (m_GpioFlg)
    {
      try
      {
        setDownGpio();
        Log.i("CY", "Gpio status = " + getGpioStatus());
      }
      catch (IOException e1)
      {
        Log.i("CY", "Close serial port abnormally!!!");
        e1.printStackTrace();
      }
    }

    if (m_SerialPort != null)
    {
      try
      {
        m_OutputStream.close();
        m_InputStream.close();
      }
      catch (IOException e)
      {
        Log.i("CY", "Close serial port abnormally!!!");
        e.printStackTrace();
      }

      m_SerialPort.close();
      m_SerialPort = null;
    }

    m_FirstOpen = false;
    m_CurrentSize = 0;

    Log.i("CY", "Close serial port successfully!!!");
  }

  private static String getComPath()
  {
    Log.i("CY", "Enter function getComPath().");
    return m_Path;
  }

  private static String getGpioDev()
  {
    Log.i("CY", "Enter function getGpioDev().");
    return m_GpioDev;
  }

  public static boolean isOpen()
  {
    Log.i("CY", "Enter function isOpen().");
    return m_IsOpen;
  }

  public static boolean getGpioFlg()
  {
    Log.i("CY", "Enter function getGpioFlg().");
    return m_GpioFlg;
  }

  public static void setGpioFlg(boolean gpoiFlg)
  {
    Log.i("CY", "Enter function setGpioFlg().");
    m_GpioFlg = gpoiFlg;
  }

  private static void setUpGpio() throws IOException
  {
    Log.i("CY", "Enter function setUpGpio().");

    FileOutputStream fw = new FileOutputStream(m_GpioDev);
    fw.write(m_PowerOn);
    fw.close();
  }

  private static void setDownGpio() throws IOException
  {
    Log.i("CY", "Enter function setDownGpio().");

    FileOutputStream fw = new FileOutputStream(m_GpioDev);
    fw.write(m_PowerDown);
    fw.close();
  }

  private static String getGpioStatus() throws IOException
  {
    Log.i("CY", "Enter function getGpioStatus().");

    BufferedReader br = null;
    FileInputStream inStream = new FileInputStream(m_GpioDev);
    br = new BufferedReader(new InputStreamReader(inStream));
    String value = br.readLine();
    inStream.close();
    return value;
  }

  public static void write(byte[] data)
  {
    Log.i("CY", "Enter function write().");

    if (!m_IsOpen)
    {
      Log.i("CY", "Write data terminated,beacuse serialport is not opened!!!");
      return;
    }

    if (m_FirstOpen)
    {
      SystemClock.sleep(1500L);
      m_FirstOpen = false;
    }

    m_CurrentSize = 0;
    try
    {
      Log.i("CY", "The data to be written is:" + DataUtils.toHexString(data) + ".");
      m_OutputStream.write(data);
    }
    catch (IOException e)
    {
      Log.i("CY", "Write data abnormally!!!");
      e.printStackTrace();
      return;
    }

    Log.i("CY", "Write data successfully!!!");
  }

  private static int read(byte[] buffer, int waitTime, int interval)
  {
    Log.i("CY", "Enter function read(byte buffer[], int waitTime, int interval).");

    if (!m_IsOpen)
    {
      Log.i("CY", "Read data terminated,beacuse serialport is not opened!!!");
      return 0;
    }

    int sleepTime = 5;
    int length = waitTime / sleepTime;
    boolean isStop = false;

    for (int i = 0; i < length; i++)
    {
      if (m_CurrentSize != 0)
        break;
      SystemClock.sleep(sleepTime);
    }

    if (m_CurrentSize > 0)
    {
      long lastTime = System.currentTimeMillis();
      int lastRecSize = 0;

      while ((!isStop) && (m_IsOpen))
      {
        if (m_CurrentSize > lastRecSize)
        {
          lastRecSize = m_CurrentSize;
          lastTime = System.currentTimeMillis();
        }
        else if ((m_CurrentSize == lastRecSize) && (System.currentTimeMillis() - lastTime >= interval))
        {
          Log.i("CY", "Read data successfully,the length of data has been read is:" + m_CurrentSize);
          isStop = true;
        }
      }

      if (m_CurrentSize <= buffer.length)
      {
        System.arraycopy(m_Buffer, 0, buffer, 0, m_CurrentSize);
      }

    }
    else
    {
      SystemClock.sleep(100L);
    }

    return m_CurrentSize;
  }

  private synchronized int readFixedLength(byte[] buffer, int waittime, int requestLength)
  {
    Log.i("CY", "Enter function readFixedLength().");

    return readFixedLength(buffer, waittime, requestLength, 15);
  }

  private synchronized int readFixedLength(byte[] buffer, int waitTime, int reqLen, int interval)
  {
    Log.i("CY", "Enter function readFixedLength().");

    if (!m_IsOpen)
    {
      Log.i("CY", "Read data terminated,beacuse serialport is not opened!!!");
      return 0;
    }

    int sleepTime = 5;
    int length = waitTime / sleepTime;
    boolean isStop = false;

    for (int i = 0; i < length; i++)
    {
      if (m_CurrentSize != 0)
        break;
      SystemClock.sleep(sleepTime);
    }

    if (m_CurrentSize > 0)
    {
      long lastTime = System.currentTimeMillis();
      int lastRecvSize = 0;

      while ((!isStop) && (m_IsOpen))
      {
        if (m_CurrentSize == reqLen)
        {
          Log.i("CY", "Read data successfully,the length of data has been read is:" + m_CurrentSize);
          isStop = true;
        }
        else if (m_CurrentSize > lastRecvSize)
        {
          lastRecvSize = m_CurrentSize;
          lastTime = System.currentTimeMillis();
        }
        else if ((m_CurrentSize == lastRecvSize) && (System.currentTimeMillis() - lastTime >= interval))
        {
          Log.i("CY", "Read data successfully,the length of data has been read is:" + m_CurrentSize);
          isStop = true;
        }

      }

      if (m_CurrentSize <= buffer.length)
      {
        System.arraycopy(m_Buffer, 0, buffer, 0, m_CurrentSize);
      }

    }
    else
    {
      SystemClock.sleep(100L);
    }

    return m_CurrentSize;
  }

  private static short iso7816CalcLRC(byte[] cmd, int len)
  {
    Log.i("CY", "Enter function DataUtils-iso7816CalcLRC()");
    int lenBuf = cmd.length;
    if ((lenBuf == 0) || (cmd == null))
    {
      return -1;
    }
    short lrc = 0;
    for (int i = 0; i < len; i++)
    {
      lrc = (short)(lrc ^ cmd[i]);
      lrc = (short)(lrc & 0xFF);
    }
    return lrc;
  }

  private static byte[] getCmd(byte[] srcCmd, int len, int cmdType)
  {
    Log.i("CY", "Enter function DataUtils-getCmd()");
    byte[] cmdHead = { (byte)cmdType, 0, (byte)len };
    byte[] cmdTmp = new byte[3 + len];
    System.arraycopy(cmdHead, 0, cmdTmp, 0, 3);
    System.arraycopy(srcCmd, 0, cmdTmp, 3, len);
    int lrc = iso7816CalcLRC(cmdTmp, len + 3);
    byte[] cmd = new byte[len + 3 + 1];
    System.arraycopy(cmdTmp, 0, cmd, 0, len + 3);
    cmd[(len + 3)] = ((byte)lrc);
    Log.i("CY", "To get cmd " + DataUtils.toHexString(cmd));
    return cmd;
  }

  private static class ReadThread extends Thread
  {
    public void run()
    {
      Log.i("CY", "Enter function ReadThread-run().");

      byte[] data = new byte[512];

      while ((!isInterrupted()) && (IcInterface.m_IsOpen))
      {
        int length = 0;
        try
        {
          if (IcInterface.m_InputStream == null)
          {
            Log.i("CY", "Error,InputStream is null!!!");
            return;
          }

          length = IcInterface.m_InputStream.read(data);
          if (length > 0)
          {
            System.arraycopy(data, 0, IcInterface.m_Buffer, IcInterface.m_CurrentSize, length);

            IcInterface.m_CurrentSize += length;

            Log.i("CY", "The currentSize is:" + IcInterface.m_CurrentSize + ", the length is:" + length);
          }

        }
        catch (IOException e)
        {
          Log.i("CY", "Read data abnormally!!!");
          e.printStackTrace();
          return;
        }
      }
    }
  }
  /**
	 * 函数说明：组装数据
	 * @param data
	 * @return
	 */
	private static byte[] makeProcData(byte[] data)
	{
		byte[] tmp = new byte[data.length + 4 + 1 + 1];
		tmp[0] = (byte)0xca;
		tmp[1] = (byte)0xdf;
		tmp[2] = (byte)0x00;
		tmp[3] = (byte)0x35;
		tmp[4] = (byte) data.length;
		System.arraycopy(data, 0, tmp, 5, data.length);
		tmp[data.length + 4 + 1] = (byte)0xe3;
		
		return tmp;
	}
	private static byte[] getRespose(int length){
		byte[] tmp = {(byte)0x00,(byte)0xc0,(byte)0x00,(byte)0x00,(byte)length};
		return makeProcData(tmp);
	}
}