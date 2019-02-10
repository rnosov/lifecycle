package com.zchr.util;

public class ScreenShowInfo {

	//左对齐 
	public static final int SCREEN_SHOW_ALIGN_LEFT = 0xF1;

	//左对齐 
	public static final int SCREEN_SHOW_ALIGN_RIGHT = 0xF2;

	//居中对齐
	public static final int SCREEN_SHOW_ALIGN_CENTER = 0xF3;

	//清除行号
	private int m_iClearLine;

	//显示行号
	private int m_iShowLine;

	//显示行信息
	private String m_strShowInfo;

	//对齐方式
	private int m_iAlignMode;

	public int getClearLine() {
		return m_iClearLine;
	}

	public void setClearLine(int i_iClearLine) {
		this.m_iClearLine = i_iClearLine;
	}

	public int getShowLine() {
		return m_iShowLine;
	}

	public void setShowLine(int i_iShowLine) {
		this.m_iShowLine = i_iShowLine;
	}

	public String getShowInfo() {
		return m_strShowInfo;
	}

	public void setShowInfo(String i_strShowInfo) {
		this.m_strShowInfo = i_strShowInfo;
	}

	public void setAlignMode(int i_iAlignMode) {
		this.m_iAlignMode = i_iAlignMode;
	}

	public int getAlignMode() {
		return m_iAlignMode;
	}
}
