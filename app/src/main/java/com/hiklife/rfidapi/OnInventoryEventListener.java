package com.hiklife.rfidapi;

import java.util.EventListener;

public interface OnInventoryEventListener extends EventListener {
	void RadioInventory(InventoryEvent event); //自定义的实现方法
}
