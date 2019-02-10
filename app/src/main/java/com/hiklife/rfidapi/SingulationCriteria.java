package com.hiklife.rfidapi;

public class SingulationCriteria {
	public SingulationCriteriaStatus status;
    public matchType match;
    public int offset;
    public int count;
    public byte[] mask;

    public SingulationCriteria()
    {
        status = SingulationCriteriaStatus.Disabled;
        match = matchType.Inverse;
        offset = 0;
        count = 0;
        mask = new byte[62];
    }
}
