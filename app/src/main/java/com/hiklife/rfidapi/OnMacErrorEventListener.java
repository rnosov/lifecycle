package com.hiklife.rfidapi;

import java.util.EventListener;

public interface OnMacErrorEventListener extends EventListener {
	void RadioMacError(MacErrorEvent event); //自定义的实现方法
}
