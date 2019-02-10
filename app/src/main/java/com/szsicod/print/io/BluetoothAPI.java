package com.szsicod.print.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

public class BluetoothAPI implements InterfaceAPI{
	private Context context;
	  public static final int WRITEBYTEMAX = 4096;
	  public static final int READBYTEMAX = 1024;
	  private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	  private BluetoothAdapter btAdapter;
	  private BluetoothDevice btDevice;
	  private BluetoothSocket btSocket;
	  private OutputStream os = null;
	  private byte[] mTempData = null;
	  private int mReadbyte = 0;
	  private Boolean mBExceptionBoolean = Boolean.valueOf(false);
	  public static final int READTIMEOUT = 90000;
	  protected int mTimeAfterRead = 100;

	  public BluetoothAPI(Context context)
	  {
	    this.context = context;
	    this.btAdapter = BluetoothAdapter.getDefaultAdapter();
	  }

	  public boolean isBTSupport()
	  {
	    if (this.btAdapter == null) {
	      return false;
	    }
	    return true;
	  }

	  public String getAddress()
	  {
	    if (this.btAdapter != null) {
	      return this.btAdapter.getAddress();
	    }
	    return null;
	  }

	  public String getName()
	  {
	    if (this.btAdapter != null) {
	      return this.btAdapter.getName();
	    }
	    return null;
	  }

	  public int getState()
	  {
	    if (this.btAdapter != null) {
	      return this.btAdapter.getState();
	    }
	    return 10;
	  }

	  @SuppressLint({"NewApi"})
	  public Boolean isConnected()
	  {
	    if (this.btSocket == null) {
	      return Boolean.valueOf(false);
	    }
	    if (Build.VERSION.SDK_INT >= 14) {
	      return Boolean.valueOf(this.btSocket.isConnected());
	    }
	    return Boolean.valueOf(false);
	  }

	  public Boolean isOpen()
	  {
	    if ((this.btDevice == null) || (!isConnected().booleanValue())) {
	      return Boolean.valueOf(false);
	    }
	    return Boolean.valueOf(true);
	  }

	  public boolean openBluetooth(int waitTime)
	  {
	    boolean ret = false;
	    try {
	      if (this.btAdapter == null) {
	        return ret;
	      }
	      if (!isEnabledBluetooth()) {
	        Intent enableIntent = new Intent(
	          "android.bluetooth.adapter.action.REQUEST_ENABLE");
	        this.context.startActivity(enableIntent);
	        if (waitTime > 0) {
	          int minTime = 150;
	          int waitCount = (waitTime + minTime - 1) / minTime;
	          for (int n = 0; n < waitCount; n++) {
	            if (isEnabledBluetooth()) {
	              ret = true;
	              break;
	            }
	            SystemClock.sleep(minTime);
	          }
	        } else if (waitTime == 0) {
	          ret = true;
	        }
	      } else {
	        ret = true;
	      }
	    } catch (Exception e) {
	      ret = false;
	    }
	    return ret;
	  }

	  public boolean isEnabledBluetooth()
	  {
	    return this.btAdapter.isEnabled();
	  }

	  public boolean closeBluetooth()
	  {
	    boolean result = true;
	    try {
	      if ((this.btAdapter != null) && (isEnabledBluetooth()))
	        this.btAdapter.disable();
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	      result = false;
	    }
	    return result;
	  }

	  public boolean setDiscoverable(int time)
	  {
	    if ((time <= 0) || (time > 300)) {
	      Log.e("SNBC_POS", "setDiscoverable error time:" + time);
	      return false;
	    }
	    boolean result = true;
	    if (this.btAdapter != null) {
	      Intent discoverableIntent = new Intent(
	        "android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
	      discoverableIntent.putExtra(
	        "android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", 
	        time);
	      this.context.startActivity(discoverableIntent);
	    } else {
	      result = false;
	    }
	    return result;
	  }

	  public boolean startDiscovery()
	  {
	    boolean result = true;
	    if ((this.btAdapter != null) && (this.btAdapter.getState() == 12)) {
	      if (this.btAdapter.isDiscovering()) {
	        this.btAdapter.cancelDiscovery();
	      }
	      result = this.btAdapter.startDiscovery();
	    } else {
	      result = false;
	    }
	    return result;
	  }

	  public boolean isDiscovery()
	  {
	    return this.btAdapter.isDiscovering();
	  }

	  public boolean cancelDiscovery()
	  {
	    boolean result = false;
	    if (this.btAdapter.isDiscovering()) {
	      result = this.btAdapter.cancelDiscovery();
	    }
	    return result;
	  }

	  public Set<BluetoothDevice> getBondedDevices() {
	    if (this.btAdapter != null) {
	      return this.btAdapter.getBondedDevices();
	    }
	    return null;
	  }

	  public int checkDevice(String macAddress) {
	    if (this.btAdapter == null) {
	      return 1001;
	    }
	    if (!BluetoothAdapter.checkBluetoothAddress(macAddress)) {
	      return 1001;
	    }
	    this.btDevice = this.btAdapter.getRemoteDevice(macAddress);
	    return 1000;
	  }

	  public int openDevice() {
	    if (this.btDevice == null) {
	      return -1;
	    }
	    int ret = 0;
	    try {
	      this.btSocket = this.btDevice
	        .createRfcommSocketToServiceRecord(uuid);
	      this.btSocket.connect();
	    } catch (Exception e) {
	      e.printStackTrace();
	      ret = -1;
	    }
	    return ret;
	  }

	  public int closeDevice() {
	    int result = 1000;
	    if (this.btDevice != null)
	      this.btDevice = null;
	    try
	    {
	      if (this.btSocket != null)
	        this.btSocket.close();
	    }
	    catch (IOException e) {
	      result = 1001;
	      e.printStackTrace();
	    }
	    return result;
	  }

	  public int writeBuffer(byte[] writeBuffer, int offsetSize, int writeSize, int waitTime)
	  {
	    int i = 0;
	    int res_len = 0;
	    int mode_len = 0;
	    int count = 0;
	    long tick = 0L;
	    long totulTick = 0L;
	    byte[] temp_buf = null;
	    byte[] res_buf = null;
	    if ((writeBuffer == null) || (writeSize <= 0) || (offsetSize < 0)) {
	      return 0;
	    }
	    if (getState() == 10) {
	      return 0;
	    }
	    tick = System.currentTimeMillis();
	    totulTick = tick + waitTime;
	    try {
	      this.os = this.btSocket.getOutputStream();
	    } catch (Exception e) {
	      e.printStackTrace();
	      return 0;
	    }
	    if (this.os == null) {
	      return 0;
	    }
	    byte[] des_buf = new byte[writeSize];
	    Arrays.fill(des_buf, (byte)0);
	    if (offsetSize + writeSize > writeBuffer.length) {
	      des_buf = null;
	      return 0;
	    }
	    System.arraycopy(writeBuffer, offsetSize, des_buf, 0, writeSize);
	    if (des_buf.length > 4096) {
	      temp_buf = new byte[4096];
	      Arrays.fill(temp_buf, (byte)0);
	      mode_len = des_buf.length / 4096;
	      res_len = des_buf.length % 4096;
	      for (count = 0; count < mode_len; count++) {
	        tick = System.currentTimeMillis();
	        if (tick > totulTick) {
	          temp_buf = null;
	          des_buf = null;
	          return count * 4096;
	        }
	        System.arraycopy(des_buf, count * 4096 + i, temp_buf, 0, 4096);
	        try {
	          this.os.write(temp_buf);
	        } catch (IOException e) {
	          e.printStackTrace();
	          temp_buf = null;
	          des_buf = null;
	          return count * 4096;
	        }
	      }
	      if (res_len != 0) {
	        res_buf = new byte[res_len];
	        for (i = 0; i < res_len; i++)
	          res_buf[i] = des_buf[(mode_len * 4096 + i)];
	        try
	        {
	          this.os.write(res_buf);
	        } catch (IOException e) {
	          e.printStackTrace();
	          temp_buf = null;
	          res_buf = null;
	          des_buf = null;
	          return 0;
	        }
	      }
	    } else {
	      try {
	        this.os.write(des_buf);
	      } catch (IOException e) {
	        e.printStackTrace();
	        temp_buf = null;
	        res_buf = null;
	        des_buf = null;
	        return 0;
	      }
	    }
	    temp_buf = null;
	    res_buf = null;
	    des_buf = null;
	    return writeSize;
	  }

	  public int readBuffer(byte[] readBuffer, int offsetSize, int readSize, int ReadTimeOut)
	  {
	    try {
	      return readBuffer(readBuffer, readSize, ReadTimeOut);
	    } catch (SocketTimeoutException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    } catch (InterruptedException e) {
	      e.printStackTrace();
	    }
	    return 0;
	  }

	  public int readBuffer(byte[] data, int readSize, int ReadTimeOut) throws IOException, SocketTimeoutException, InterruptedException
	  {
	    if (data == null) {
	      return 0;
	    }
	    int data_len = data.length;

	    ReadThread readThread = new ReadThread();
	    readThread.setreadSize(readSize);
	    readThread.start();

	    long beginTime = System.currentTimeMillis();
	    while ((readThread.isAlive()) && (
	      System.currentTimeMillis() < beginTime + ReadTimeOut));
	    readThread.close();
	    if ((readThread != null) && (readThread.isAlive())) {
	      readThread.interrupt();
	    }
	    if (this.mBExceptionBoolean.booleanValue()) {
	      this.mBExceptionBoolean = Boolean.valueOf(false);
	      return 0;
	    }
	    if ((this.mReadbyte > 0) && (this.mReadbyte <= data_len)) {
	      System.arraycopy(this.mTempData, 0, data, 0, this.mReadbyte);

	      Log.i("Test", new String(data));
	    }
	    return this.mReadbyte;
	  }

	  public String byte2hex(byte[] buffer)
	  {
	    String h = "";
	    for (int i = 0; i < buffer.length; i++) {
	      String temp = Integer.toHexString(buffer[i] & 0xFF);
	      if (temp.length() == 1) {
	        temp = "0" + temp;
	      }
	      h = h + " " + temp;
	    }
	    return h;
	  }

	  public void finallize()
	  {
	    closeDevice();
	    closeBluetooth();
	  }

	  private class ReadThread extends Thread
	  {
	    private volatile boolean isRun = true;
	    private int readSize = 0;

	    private ReadThread() {
	    }

	    public void run() {
	      super.run();

	      BluetoothAPI.this.mTempData = new byte[4096];
	      BluetoothAPI.this.mReadbyte = 0;
	      BluetoothAPI.this.mBExceptionBoolean = Boolean.valueOf(false);
	      while (this.readSize > BluetoothAPI.this.mReadbyte)
	        if (this.isRun)
	          try {
	            byte[] thisTempData = new byte[4096];
	            int thisReadbyte = BluetoothAPI.this.btSocket
	              .getInputStream().read(thisTempData);
	            if (thisReadbyte > 0) {
	              System.arraycopy(thisTempData, 0, 
	                BluetoothAPI.this.mTempData, 
	                BluetoothAPI.this.mReadbyte, thisReadbyte);
	            }
	            BluetoothAPI.this.mReadbyte += thisReadbyte;
	            Log.i("Blue", new String(BluetoothAPI.this.mTempData));
	          } catch (IOException e) {
	            BluetoothAPI.this.mBExceptionBoolean = 
	              Boolean.valueOf(true);
	            e.printStackTrace();
	          }
	    }

	    public void close()
	    {
	      this.isRun = false;
	    }

	    public void setreadSize(int readSize) {
	      this.readSize = readSize;
	    }
	  }
}
