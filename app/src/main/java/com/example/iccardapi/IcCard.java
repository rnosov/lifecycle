package com.example.iccardapi;

public class IcCard
{
  static
  {
    System.loadLibrary("ictrack");
  }

  public static native byte[] getTrackData();

  public static native byte[] getAid();

  public static native int getAidLength();
}