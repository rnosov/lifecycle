package com.szsicod.print.escpos;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Bmp {

	  private BmpFileHeader fileHeader = new BmpFileHeader();
	  private BmpInfoHerder infoHeader = new BmpInfoHerder();
	  private RGBQUAD[] rgbquadList = null;
	  private byte[] data = null;

	  public Bmp(Bitmap bitmap, short bitCount)
	    throws IOException
	  {
	    if (bitmap == null) {
	      return;
	    }
	    long[] tick = new long[10];
	    tick[0] = System.currentTimeMillis();
	    this.infoHeader.biSize = 40;
	    this.infoHeader.biWidth = bitmap.getWidth();
	    this.infoHeader.biHeight = bitmap.getHeight();
	    this.infoHeader.biPlanes = 1;
	    this.infoHeader.biBitCount = bitCount;
	    this.infoHeader.biCompression = 0;
	    this.infoHeader.biSizeImage = 0;
	    this.infoHeader.biXPelsPerMeter = 0;
	    this.infoHeader.biYPelsPerMeter = 0;
	    this.infoHeader.biClrUsed = 0;
	    this.infoHeader.biClrImportant = 0;
	    if (this.infoHeader.biBitCount == 1) {
	      this.rgbquadList = new RGBQUAD[2];
	      this.rgbquadList[0] = new RGBQUAD();
	      this.rgbquadList[0].rgbBlue = 0;
	      this.rgbquadList[0].rgbGreen = 0;
	      this.rgbquadList[0].rgbRed = 0;
	      this.rgbquadList[0].rgbRed = 0;
	      this.rgbquadList[1] = new RGBQUAD();
	      this.rgbquadList[1].rgbBlue = -1;
	      this.rgbquadList[1].rgbGreen = -1;
	      this.rgbquadList[1].rgbRed = -1;
	      this.rgbquadList[1].rgbRed = 0;
	    } else if (this.infoHeader.biBitCount == 4) {
	      this.rgbquadList = new RGBQUAD[16];
	    } else if (this.infoHeader.biBitCount == 8) {
	      this.rgbquadList = new RGBQUAD[256];
	    }
	    int bmpWidth = ((this.infoHeader.biWidth + 7) / 
	      this.infoHeader.biBitCount / 8 + 3) / 4 * 4;
	    int bufferSize = bmpWidth * this.infoHeader.biHeight;
	    this.fileHeader.bfType = 19778;
	    this.fileHeader.bfReserved1 = 0;
	    this.fileHeader.bfReserved2 = 0;
	    this.fileHeader.bfOffBits = (54 + this.rgbquadList.length * 4);
	    this.fileHeader.bfSize = (this.fileHeader.bfOffBits + bufferSize);
	    this.infoHeader.biSizeImage = (this.fileHeader.bfSize - this.fileHeader.bfOffBits);
	    this.data = new byte[this.fileHeader.bfSize];
	    int writeSize = 0;

	    writeSize += writeWord(this.data, writeSize, this.fileHeader.bfType);
	    writeSize += writeDword(this.data, writeSize, this.fileHeader.bfSize);

	    writeSize = writeSize + 
	      writeWord(this.data, writeSize, 
	      this.fileHeader.bfReserved1);

	    writeSize = writeSize + 
	      writeWord(this.data, writeSize, 
	      this.fileHeader.bfReserved2);
	    writeSize += writeDword(this.data, writeSize, this.fileHeader.bfOffBits);

	    writeSize += writeDword(this.data, writeSize, this.infoHeader.biSize);
	    writeSize += writeLong(this.data, writeSize, this.infoHeader.biWidth);
	    writeSize += writeLong(this.data, writeSize, this.infoHeader.biHeight);
	    writeSize += writeWord(this.data, writeSize, this.infoHeader.biPlanes);
	    writeSize += writeWord(this.data, writeSize, this.infoHeader.biBitCount);

	    writeSize = writeSize + 
	      writeDword(this.data, writeSize, 
	      this.infoHeader.biCompression);

	    writeSize = writeSize + 
	      writeDword(this.data, writeSize, 
	      this.infoHeader.biSizeImage);

	    writeSize = writeSize + 
	      writeLong(this.data, writeSize, 
	      this.infoHeader.biXPelsPerMeter);

	    writeSize = writeSize + 
	      writeLong(this.data, writeSize, 
	      this.infoHeader.biYPelsPerMeter);
	    writeSize += writeDword(this.data, writeSize, this.infoHeader.biClrUsed);

	    writeSize = writeSize + 
	      writeDword(this.data, writeSize, 
	      this.infoHeader.biClrImportant);
	    for (int n = 0; n < this.rgbquadList.length; n++)
	    {
	      writeSize = writeSize + 
	        writeByte(this.data, writeSize, 
	        this.rgbquadList[n].rgbBlue);

	      writeSize = writeSize + 
	        writeByte(this.data, writeSize, 
	        this.rgbquadList[n].rgbGreen);

	      writeSize = writeSize + 
	        writeByte(this.data, writeSize, 
	        this.rgbquadList[n].rgbRed);

	      writeSize = writeSize + 
	        writeByte(this.data, writeSize, 
	        this.rgbquadList[n].rgbReserved);
	    }
	    int[] pixels = new int[this.infoHeader.biWidth * 
	      this.infoHeader.biHeight];
	    bitmap.getPixels(pixels, 0, this.infoHeader.biWidth, 0, 0, 
	      this.infoHeader.biWidth, this.infoHeader.biHeight);
	    int nCol = 0;
	    int nRealCol = this.infoHeader.biHeight - 1;
	    int blockSize = 8;
	    int maxW = (this.infoHeader.biWidth + blockSize - 1) / blockSize;

	    tick[1] = System.currentTimeMillis();
	    for (; nCol < this.infoHeader.biHeight; nRealCol--) {
	      int colStart = writeSize + nRealCol * bmpWidth;
	      for (int wRow = 0; wRow < maxW; wRow++) {
	        int startNum = blockSize * wRow;
	        for (int n = 0; (n < blockSize) && (
	          startNum + n < this.infoHeader.biWidth); 
	          n++) {
	          int clr = pixels[
	            (startNum + n + nCol * 
	            this.infoHeader.biWidth)];
	          int gray = getGreyLevel(clr, 1.0F);
	          if (gray > 127) {
	            int cz = 1;
	            int tmp1016_1015 = colStart + wRow;
	            int tmp1119_1117 = tmp1016_1015;
	            byte[] tmp1119_1114 = this.data;
	            tmp1119_1114[tmp1119_1117] = ((byte)(tmp1119_1114[tmp1119_1117] + (byte)(cz << 7 - n)));
	          }
	        }
	      }
	      nCol++;
	    }
	    tick[2] = System.currentTimeMillis();
	    tick[3] = (tick[2] - tick[1]);
	  }

	  public byte[] getData()
	  {
	    return this.data;
	  }

	  public int getWidth()
	  {
	    return this.infoHeader.biWidth;
	  }

	  public int getHeight()
	  {
	    return this.infoHeader.biHeight;
	  }

	  public int getBitCount()
	  {
	    return this.infoHeader.biBitCount;
	  }

	  private int writeWord(byte[] stream, int start, int value) throws IOException
	  {
	    byte[] b = new byte[2];
	    b[0] = ((byte)(value & 0xFF));
	    b[1] = ((byte)(value >> 8 & 0xFF));
	    System.arraycopy(b, 0, stream, start, 2);

	    return 2;
	  }

	  private int writeDword(byte[] stream, int start, long value) throws IOException
	  {
	    byte[] b = new byte[4];
	    b[0] = ((byte)(int)(value & 0xFF));
	    b[1] = ((byte)(int)(value >> 8 & 0xFF));
	    b[2] = ((byte)(int)(value >> 16 & 0xFF));
	    b[3] = ((byte)(int)(value >> 24 & 0xFF));
	    System.arraycopy(b, 0, stream, start, 4);

	    return 4;
	  }

	  private int writeLong(byte[] stream, int start, long value) throws IOException
	  {
	    byte[] b = new byte[4];
	    b[0] = ((byte)(int)(value & 0xFF));
	    b[1] = ((byte)(int)(value >> 8 & 0xFF));
	    b[2] = ((byte)(int)(value >> 16 & 0xFF));
	    b[3] = ((byte)(int)(value >> 24 & 0xFF));
	    System.arraycopy(b, 0, stream, start, 4);

	    return 4;
	  }

	  private int writeByte(byte[] stream, int start, byte value) throws IOException
	  {
	    stream[start] = value;
	    return 1;
	  }

	  public static int getGreyLevel(int pixel, float intensity) {
	    float alpha = Color.alpha(pixel);
	    if (alpha < 50.0F) {
	      return 255;
	    }
	    float red = Color.red(pixel);
	    float green = Color.green(pixel);
	    float blue = Color.blue(pixel);
	    float parcial = red + green + blue;
	    parcial = (float)(parcial / 3.0D);
	    int gray = (int)(parcial * intensity);
	    if (gray > 255) {
	      gray = 255;
	    }
	    return gray;
	  }

	  public class BmpFileHeader
	  {
	    public short bfType;
	    public int bfSize;
	    public short bfReserved1;
	    public short bfReserved2;
	    public int bfOffBits;

	    public BmpFileHeader()
	    {
	    }
	  }

	  public class BmpInfoHerder
	  {
	    public short biSize;
	    public int biWidth;
	    public int biHeight;
	    public short biPlanes;
	    public short biBitCount;
	    public int biCompression;
	    public int biSizeImage;
	    public int biXPelsPerMeter;
	    public int biYPelsPerMeter;
	    public int biClrUsed;
	    public int biClrImportant;

	    public BmpInfoHerder()
	    {
	    }
	  }

	  public class RGBQUAD
	  {
	    public byte rgbBlue;
	    public byte rgbGreen;
	    public byte rgbRed;
	    public byte rgbReserved;

	    public RGBQUAD()
	    {
	    }
	  }
}
