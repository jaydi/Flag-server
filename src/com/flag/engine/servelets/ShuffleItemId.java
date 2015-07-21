package com.flag.engine.servelets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flag.engine.models.Item;
import com.flag.engine.models.ItemId;
import com.flag.engine.models.PMF;

public class ShuffleItemId extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(ShuffleItemId.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		long startTime = new Date().getTime();
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Item.class);
		query.setFilter("available == yes");
		query.declareParameters("boolean yes");
		List<Item> items = (List<Item>) pm.newQuery(query).execute(true);

		log.warning("item da time: " + (new Date().getTime() - startTime) + "ms");

		List<ItemId> itemIds = new ArrayList<ItemId>();
		for (Item item : items)
			itemIds.add(new ItemId(item.getId(), item.getSex()));
		Collections.shuffle(itemIds);

		log.warning("item id shuffle time: " + (new Date().getTime() - startTime) + "ms");

		query = pm.newQuery(ItemId.class);
		List<ItemId> oldItemIds = (List<ItemId>) pm.newQuery(query).execute();

		pm.makePersistentAll(itemIds);
		pm.deletePersistentAll(oldItemIds);

		log.warning("item id dw time: " + (new Date().getTime() - startTime) + "ms");

		pm.close();
	}
}
