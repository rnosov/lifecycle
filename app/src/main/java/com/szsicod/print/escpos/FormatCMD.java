package com.szsicod.print.escpos;

import java.io.IOException;

import android.graphics.Bitmap;

public class FormatCMD {
	private int byteArrayToIntLowFirst(byte[] b, int offset)
	  {
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	      int shift = i * 8;
	      value += ((b[(i + offset)] & 0xFF) << shift);
	    }
	    return value;
	  }

	  public byte[] BitmapToBMPData(Bitmap bitmap)
	    throws IOException
	  {
	    Bmp bmp = new Bmp(bitmap, (short)1);

	    return bmp.getData();
	  }

	  public int jbitmap2cmd(byte[] data, int dataSize, byte[] outData, int outDataMaxSize)
	  {
	    int Readed = 0;
	    int bmihbiWidth = 0;
	    int bmihbiHeight = 0;
	    int bmfhbfOffBits = 0;

	    int outSize = 0;

	    byte[] pInData = data;
	    byte[] pOutData = outData;
	    int i = 0;
	    int j = 0;
	    int m = 0;
	    int foff = 0;
	    if ((pInData == null) || (pOutData == null) || (dataSize <= 0) || 
	      (outDataMaxSize <= 0)) {
	      return -1;
	    }
	    if (dataSize <= 64) {
	      return -2;
	    }
	    bmihbiWidth = byteArrayToIntLowFirst(pInData, 18);
	    bmihbiHeight = byteArrayToIntLowFirst(pInData, 22);
	    bmfhbfOffBits = byteArrayToIntLowFirst(pInData, 10);

	    int iWidth = bmihbiWidth;
	    int iHeight = bmihbiHeight;

	    int ByteWidth = (iWidth + 31) / 32 * 4;

	    int TrueByteWidth = (iWidth + 7) / 8;
	    int TrueHeight = iHeight;
	    Readed = 0;

	    pOutData[0] = 29;
	    pOutData[1] = 118;
	    pOutData[2] = 48;
	    pOutData[3] = 0;
	    pOutData[4] = ((byte)(TrueByteWidth % 256));
	    pOutData[5] = ((byte)(TrueByteWidth / 256));
	    pOutData[6] = ((byte)(iHeight % 256));
	    pOutData[7] = ((byte)(iHeight / 256));

	    outSize += 8;

	    for (i = 0; i < iHeight; i++) {
	      foff = bmfhbfOffBits + (iHeight - 1 - i) * ByteWidth;
	      System.arraycopy(pInData, foff, pOutData, 8 + Readed * 
	        TrueByteWidth, TrueByteWidth);
	      Readed++;
	      outSize += TrueByteWidth;
	      if ((Readed == TrueHeight) || (i == iHeight - 1)) {
	        break;
	      }
	    }
	    for (j = 0; j < Readed; j++) {
	      for (m = 0; m < TrueByteWidth; m++) {
	        if (m == TrueByteWidth - 1) {
	          byte[] mask = { -128, -64, -32, -16, -8, -4, -2, -1 };
	          if (iWidth % 8 > 0)
	            pOutData[(8 + j * TrueByteWidth + m)] = 
	              ((byte)((pOutData[
	              (8 + 
	              j * TrueByteWidth + m)] ^ 0xFFFFFFFF) & mask[(iWidth % 8 - 1)]));
	          else
	            pOutData[(8 + j * TrueByteWidth + m)] = 
	              ((byte)(pOutData[
	              (8 + 
	              j * TrueByteWidth + m)] ^ 0xFFFFFFFF));
	        }
	        else {
	          pOutData[(8 + j * TrueByteWidth + m)] = 
	            ((byte)(pOutData[
	            (8 + 
	            j * TrueByteWidth + m)] ^ 0xFFFFFFFF));
	        }
	      }
	    }
	    outData = pOutData;
	    return outSize;
	  }
}
