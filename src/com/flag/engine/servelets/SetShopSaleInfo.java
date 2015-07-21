package com.flag.engine.servelets;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flag.engine.models.Item;
import com.flag.engine.models.PMF;
import com.flag.engine.models.Shop;

public class SetShopSaleInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(SetShopSaleInfo.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		long startTime = new Date().getTime();
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Item.class);
		List<Item> items = (List<Item>) pm.newQuery(query).execute();

		log.warning("item da time: " + (new Date().getTime() - startTime) + "ms");

		query = pm.newQuery(Shop.class);
		List<Shop> shops = (List<Shop>) pm.newQuery(query).execute();

		log.warning("shop da time: " + (new Date().getTime() - startTime) + "ms");

		Set<Long> saleShopIds = new HashSet<Long>();
		for (Item item : items)
			if (item.getSale() > 0)
				saleShopIds.add(item.getShopId());

		log.warning("sale shop process time: " + (new Date().getTime() - startTime) + "ms");

		for (Shop shop : shops)
			if (saleShopIds.contains(shop.getId()))
				shop.setOnSale(true);
			else
				shop.setOnSale(false);

		log.warning("sale shop dw time: " + (new Date().getTime() - startTime) + "ms");

		pm.close();
	}
}
