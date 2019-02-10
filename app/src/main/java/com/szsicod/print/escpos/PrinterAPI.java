package com.szsicod.print.escpos;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.szsicod.print.io.InterfaceAPI;

import android.graphics.Bitmap;
import android.util.Log;

public class PrinterAPI {
	public static final int SUCCESS = 0;
	  public static final int FAIL = -1;
	  public static final int ERR_PARAM = -2;
	  public static final int STATE_NORMAL = 303174166;
	  public static final int STATE_CoverOpen = 1024;
	  public static final int STATE_PaperNearEnd = 201326592;
	  public static final int STATE_PaperEnd = 1610612736;
	  private static final int cmdSizeMax = 24;
	  private static final int waitTimeDefault = 2000;
	  private InterfaceAPI mIO = null;
	  private byte[] mCmd = new byte[24];

	  public synchronized int connect(InterfaceAPI io)
	  {
	    if (!io.isOpen().booleanValue()) {
	      if (io.openDevice() != 0) {
	        return -2;
	      }
	      if (!io.isOpen().booleanValue()) {
	        return -1;
	      }
	    }
	    this.mIO = io;
	    return 0;
	  }

	  public synchronized int disconnect()
	  {
	    if (this.mIO == null) {
	      return -1;
	    }
	    if ((this.mIO.isOpen().booleanValue()) && (this.mIO.closeDevice() != 0)) {
	      return -1;
	    }
	    return 0;
	  }

	  public synchronized int writeIO(byte[] writeBuffer, int offsetSize, int writeSize, int waitTime)
	  {
	    if (this.mIO == null) {
	      return -1;
	    }
	    int ret = this.mIO.writeBuffer(writeBuffer, offsetSize, writeSize, 
	      waitTime);
	    if (ret < 0) {
	      return -1;
	    }
	    return ret;
	  }

	  private synchronized int verifyWriteIO(byte[] writeBuffer, int offsetSize, int writeSize, int waitTime)
	  {
	    if (writeSize != writeIO(writeBuffer, offsetSize, writeSize, waitTime)) {
	      return -1;
	    }
	    return 0;
	  }

	  public synchronized int readIO(byte[] readBuffer, int offsetSize, int readSize, int waitTime)
	  {
	    if (this.mIO == null) {
	      return -1;
	    }
	    int ret = this.mIO.readBuffer(readBuffer, offsetSize, readSize, 
	      waitTime);
	    if (ret < 0) {
	      return -1;
	    }
	    return ret;
	  }

	  public synchronized int init()
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 65;
	    return writeIO(this.mCmd, 0, 2, 2000);
	  }

	  public synchronized int getStatus()
	  {
	    int status = 0;

	    this.mCmd[0] = 16;
	    this.mCmd[1] = 4;
	    this.mCmd[2] = 1;
	    this.mCmd[3] = 16;
	    this.mCmd[4] = 4;
	    this.mCmd[5] = 2;
	    this.mCmd[6] = 16;
	    this.mCmd[7] = 4;
	    this.mCmd[8] = 3;
	    this.mCmd[9] = 16;
	    this.mCmd[10] = 4;
	    this.mCmd[11] = 4;
	    int writeSize = 12;
	    if (verifyWriteIO(this.mCmd, 0, writeSize, 2000) != 0) {
	      return -1;
	    }
	    int readSize = 4;
	    if (readSize != readIO(this.mCmd, 0, readSize, 2000)) {
	      return -1;
	    }
	    status = this.mCmd[0];
	    status += (this.mCmd[1] << 8);
	    status += (this.mCmd[2] << 16);
	    status += (this.mCmd[3] << 24);
	    return status;
	  }

	  public synchronized int printString(String text, String charsetName)
	    throws UnsupportedEncodingException
	  {
	    byte[] textData = text.getBytes("GBK");
	    verifyWriteIO(textData, 0, textData.length, 2000);

	    return printFeed();
	  }

	  public synchronized int printRasterBitmap(Bitmap bitmap)
	    throws IOException
	  {
	    Bmp bp = new Bmp(bitmap, (short)1);
	    Log.e("mew", "bitmap:" + bitmap.getHeight() + "," + bitmap.getWidth());
	    Log.e("mew", "bmp lenth:" + bp.getData().length);

	    byte[] data = bp.getData();

	    int outDataMaxSize = data.length + 128;
	    byte[] outData = new byte[outDataMaxSize];

	    FormatCMD fc = new FormatCMD();
	    int outSize = fc
	      .jbitmap2cmd(data, data.length, outData, outDataMaxSize);
	    Log.e("mew", "outSize  " + outSize);
	    if (outSize < 0) {
	      return -1;
	    }
	    return verifyWriteIO(outData, 0, outSize, 9000);
	  }

	  public synchronized int printQRCode(String text, int type, int size)
	  {
	    int len = text.length();
	    if (len <= 0) {
	      return -2;
	    }
	    byte[] qrcode = text.getBytes();
	    if (qrcode.length != len) {
	      return -2;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 40;
	    this.mCmd[2] = 107;
	    this.mCmd[3] = ((byte)((len + 3) % 256));
	    this.mCmd[4] = ((byte)((len + 3) / 256));
	    this.mCmd[5] = 49;
	    this.mCmd[6] = 80;
	    this.mCmd[7] = 48;
	    if (verifyWriteIO(this.mCmd, 0, 8, 2000) != 0) {
	      return -1;
	    }
	    if (verifyWriteIO(qrcode, 0, len, 2000) != 0) {
	      return -1;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 40;
	    this.mCmd[2] = 107;
	    this.mCmd[3] = 3;
	    this.mCmd[4] = 0;
	    this.mCmd[5] = 49;
	    this.mCmd[6] = 81;
	    this.mCmd[7] = 48;
	    if (verifyWriteIO(this.mCmd, 0, 8, 2000) != 0) {
	      return -1;
	    }
	    return 0;
	  }

	  public synchronized int setPrintQRCodeType(int type)
	  {
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 40;
	    this.mCmd[2] = 107;
	    this.mCmd[3] = 4;
	    this.mCmd[4] = 0;
	    this.mCmd[5] = 49;
	    this.mCmd[6] = 65;
	    this.mCmd[7] = ((byte)(48 + type));
	    this.mCmd[8] = 0;

	    return verifyWriteIO(this.mCmd, 0, 9, 2000);
	  }

	  public synchronized int setPrintQRCodeSize(int size)
	  {
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 40;
	    this.mCmd[2] = 107;
	    this.mCmd[3] = 3;
	    this.mCmd[4] = 0;
	    this.mCmd[5] = 49;
	    this.mCmd[6] = 67;
	    this.mCmd[7] = ((byte)size);

	    return verifyWriteIO(this.mCmd, 0, 8, 2000);
	  }

	  public synchronized int setPrintQRCodeErrCL(int level)
	  {
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 40;
	    this.mCmd[2] = 107;
	    this.mCmd[3] = 3;
	    this.mCmd[4] = 0;
	    this.mCmd[5] = 49;
	    this.mCmd[6] = 69;
	    this.mCmd[7] = ((byte)(71 + level));

	    return verifyWriteIO(this.mCmd, 0, 8, 2000);
	  }

	  public synchronized int printFeed()
	  {
	    this.mCmd[0] = 10;

	    return verifyWriteIO(this.mCmd, 0, 1, 2000);
	  }

	  public synchronized int printAndBackToStd()
	  {
	    this.mCmd[0] = 12;

	    return verifyWriteIO(this.mCmd, 0, 1, 2000);
	  }

	  public synchronized int printerRequestsRealTime(int type)
	  {
	    if ((type != 1) && (type != 2)) {
	      return -2;
	    }
	    this.mCmd[0] = 16;
	    this.mCmd[1] = 5;
	    this.mCmd[2] = ((byte)type);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setFontStyle(int type)
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 33;
	    this.mCmd[2] = ((byte)type);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setCharRightSpace(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 32;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setEnableUnderLine(int enable)
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 45;
	    this.mCmd[2] = ((byte)enable);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setDefaultLineSpace()
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 50;

	    return verifyWriteIO(this.mCmd, 0, 2, 2000);
	  }

	  public synchronized int setLineSpace(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 51;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setEmphasizedMode(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 69;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setOverlapMode(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 71;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int printAndFeedPaper(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 74;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setInterCharSet(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 82;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setAlignMode(int type)
	  {
	    if ((type != 0) && (type != 1) && (type != 2) && (type != 48) && 
	      (type != 49) && (type != 50)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 97;
	    this.mCmd[2] = ((byte)type);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setPaperSensor(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 99;
	    this.mCmd[2] = 51;
	    this.mCmd[3] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 4, 2000);
	  }

	  public synchronized int setSensorToStopPrint(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 99;
	    this.mCmd[2] = 52;
	    this.mCmd[3] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 4, 2000);
	  }

	  public synchronized int setEnablePanelButton(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 99;
	    this.mCmd[2] = 53;
	    this.mCmd[3] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 4, 2000);
	  }

	  public synchronized int printAndFeedLine(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 100;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setCharCodeTable(int n)
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 116;
	    if (((n >= 0) && (n <= 5)) || ((n >= 16) && (n <= 19)) || (n == 255))
	      this.mCmd[2] = ((byte)n);
	    else {
	      return -2;
	    }
	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int feedToStartPos()
	  {
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 12;

	    return verifyWriteIO(this.mCmd, 0, 2, 2000);
	  }

	  public synchronized int setCharSize(int hsize, int vsize)
	  {
	    int Width = 0;
	    if (hsize == 0) {
	      Width = 0;
	    }
	    if (hsize == 1) {
	      Width = 16;
	    }
	    if (hsize == 2) {
	      Width = 32;
	    }
	    if (hsize == 3) {
	      Width = 48;
	    }
	    if (hsize == 4) {
	      Width = 64;
	    }
	    if (hsize == 5) {
	      Width = 80;
	    }
	    if (hsize == 6) {
	      Width = 96;
	    }
	    if (hsize == 7) {
	      Width = 112;
	    }
	    if (Width <= 0) {
	      Width = 0;
	    }
	    if (Width >= 112) {
	      Width = 112;
	    }
	    if (vsize <= 0) {
	      vsize = 0;
	    }
	    if (vsize >= 7) {
	      vsize = 7;
	    }
	    int Mul = Width + vsize;

	    this.mCmd[0] = 29;
	    this.mCmd[1] = 33;
	    this.mCmd[2] = ((byte)Mul);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int doTestPrint(int n, int m)
	  {
	    if ((n < 0) || (n > 50)) {
	      return -2;
	    }
	    if ((n > 2) && (n < 48)) {
	      return -2;
	    }
	    if ((m < 1) || (m > 51)) {
	      return -2;
	    }
	    if ((m > 3) && (m < 49)) {
	      return -2;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 40;
	    this.mCmd[2] = 65;
	    this.mCmd[3] = 2;
	    this.mCmd[4] = 0;
	    this.mCmd[5] = ((byte)n);
	    this.mCmd[6] = ((byte)m);

	    return verifyWriteIO(this.mCmd, 0, 7, 2000);
	  }

	  public synchronized int setLeftMargin(int nL, int nH)
	  {
	    if ((nL < 0) || (nL > 255) || (nH < 0) || (nH > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 76;
	    this.mCmd[2] = ((byte)nL);
	    this.mCmd[3] = ((byte)nH);

	    return verifyWriteIO(this.mCmd, 0, 4, 2000);
	  }

	  public synchronized int cutPaper(int m, int n)
	  {
	    int k = 0;
	    if ((m != 0) && (m != 1) && (m != 48) && (m != 49) && (m != 66)) {
	      return -2;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 86;
	    if (m == 66) {
	      if ((n < 0) || (n > 255)) {
	        return -2;
	      }
	      this.mCmd[2] = ((byte)m);
	      this.mCmd[3] = ((byte)n);
	      k = 4;
	    } else {
	      this.mCmd[2] = ((byte)m);
	      k = 3;
	    }
	    return verifyWriteIO(this.mCmd, 0, k, 2000);
	  }

	  public synchronized int setPrnAreaWidth(int nL, int nH)
	  {
	    if ((nL < 0) || (nL > 255) || (nH < 0) || (nH > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 87;
	    this.mCmd[2] = ((byte)nL);
	    this.mCmd[3] = ((byte)nH);

	    return verifyWriteIO(this.mCmd, 0, 4, 2000);
	  }

	  public synchronized int setEnableSmoothPrn(int n)
	  {
	    if ((n < 0) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 98;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setBarCodeHeight(int n)
	  {
	    if ((n < 1) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 104;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int printBarCode(int m, int n, String barcode)
	  {
	    int j = 0;
	    int k = 0;
	    int i = barcode.length();
	    byte[] data = barcode.getBytes();
	    if (i <= 0) {
	      return -2;
	    }
	    if ((m < 0) || (m > 75) || ((m > 8) && (m < 65))) {
	      return -2;
	    }
	    if ((n < 1) || (n > 255)) {
	      return -2;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 107;
	    this.mCmd[2] = ((byte)m);
	    if ((m >= 65) && (m <= 75)) {
	      this.mCmd[3] = ((byte)n);
	    }
	    switch (m) {
	    case 0:
	      if ((i < 11) || (i > 12)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 3)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 1:
	      if ((i < 11) || (i > 12)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 3)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 2:
	      if ((i < 12) || (i > 13)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 3)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 3:
	      if ((i < 7) || (i > 8)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 3)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 4:
	      if ((i < 1) || (i > 255)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code == 32) || (code == 36) || (code == 37) || 
	          ((code >= 45) && (code <= 57)) || 
	          ((code >= 65) && (code <= 90)) || (code == 43)) {
	          this.mCmd[(j + 3)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 5:
	      if ((i < 1) || (i % 2 != 0)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 3)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 6:
	      if ((i < 1) || (i > 255)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code == 36) || ((code >= 45) && (code <= 58)) || 
	          ((code >= 65) && (code <= 90)) || (code == 43)) {
	          this.mCmd[(j + 3)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 7:
	      if ((i < 12) && (i > 13)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 3)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 8:
	      if ((i < 7) || (i > 8)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 3)] = code;
	          j++;
	          k++;
	        }
	      }
	    }
	    switch (m) {
	    case 65:
	      if ((i < 11) || (i > 12)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 66:
	      if ((i < 11) || (i > 12)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 67:
	      if ((i < 12) && (i > 13)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 68:
	      if ((i < 7) || (i > 8)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 69:
	      if ((i < 1) || (i > 255)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code == 32) || (code == 36) || (code == 37) || 
	          ((code >= 45) && (code <= 57)) || 
	          ((code >= 65) && (code <= 90)) || (code == 43)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 70:
	      if ((i < 1) || (i > 255) || (i % 2 != 0)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 71:
	      if ((i < 1) || (i > 255)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code == 36) || ((code >= 45) && (code <= 58)) || 
	          ((code >= 65) && (code <= 90)) || (code == 43)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 72:
	      if ((i < 1) || (i > 255)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code <= 127) || (code >= 0)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 73:
	      if ((i < 1) || (i > 255)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code <= 127) || (code >= 0)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 74:
	      if ((i < 12) || (i > 13)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	      break;
	    case 75:
	      if ((i < 7) || (i > 8)) {
	        return -2;
	      }
	      while (j < i) {
	        byte code = data[k];
	        if ((code >= 48) && (code <= 57)) {
	          this.mCmd[(j + 4)] = code;
	          j++;
	          k++;
	        }
	      }
	    }
	    if ((m >= 0) && (m <= 8)) {
	      this.mCmd[(j + 3)] = 0;
	    }
	    this.mCmd[(j + 4)] = 10;

	    return verifyWriteIO(this.mCmd, 0, j + 5, 2000);
	  }

	  public synchronized int getPrinterStatus(int n)
	  {
	    if ((n != 1) && (n != 49)) {
	      return -2;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 114;
	    this.mCmd[2] = ((byte)n);
	    if (verifyWriteIO(this.mCmd, 0, 3, 2000) != 0) {
	      return -1;
	    }
	    if (1 != readIO(this.mCmd, 0, 1, 2000)) {
	      return -1;
	    }
	    int ret = this.mCmd[0];
	    return ret;
	  }

	  public synchronized int setBarCodeWidth(int n)
	  {
	    if ((n < 2) || (n > 6)) {
	      return -1;
	    }
	    this.mCmd[0] = 29;
	    this.mCmd[1] = 119;
	    this.mCmd[2] = ((byte)n);

	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int pagePrintAndBack2Standard()
	  {
	    this.mCmd[0] = 12;
	    return verifyWriteIO(this.mCmd, 0, 1, 2000);
	  }

	  public synchronized int pageRemoveAllData()
	  {
	    this.mCmd[0] = 24;
	    return verifyWriteIO(this.mCmd, 0, 1, 2000);
	  }

	  public synchronized int pagePrint()
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 12;
	    return verifyWriteIO(this.mCmd, 0, 2, 2000);
	  }

	  public synchronized int pageMode()
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 76;
	    return verifyWriteIO(this.mCmd, 0, 2, 2000);
	  }

	  public synchronized int standardMode()
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 83;
	    return verifyWriteIO(this.mCmd, 0, 2, 2000);
	  }

	  public synchronized int pageSelectDirection(int n)
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 84;
	    this.mCmd[2] = ((byte)n);
	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }

	  public synchronized int setRotate(int n)
	  {
	    this.mCmd[0] = 27;
	    this.mCmd[1] = 86;
	    this.mCmd[2] = ((byte)n);
	    return verifyWriteIO(this.mCmd, 0, 3, 2000);
	  }
}
