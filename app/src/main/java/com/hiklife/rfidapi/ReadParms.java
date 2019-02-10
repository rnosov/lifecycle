package com.hiklife.rfidapi;

public class ReadParms {
	public MemoryBank memBank;
    public short offset;
    public short length;
    public int accesspassword;

    public ReadParms()
    {
        memBank = MemoryBank.Reserved;
        offset = 0;
        length = 0;
        accesspassword = 0;
    }
}
