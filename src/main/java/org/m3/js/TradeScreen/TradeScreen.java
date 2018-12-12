package org.m3.js.TradeScreen;

import org.m3.js.Orders.Order;

import java.io.IOException;

public interface TradeScreen {
	void newOrder(Order order);
	void acceptOrder(Order order);
	void declineOrder(Order order);
	void sliceOrder(Order order);
	void price(Order order);
}
