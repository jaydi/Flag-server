package com.flag.engine.servelets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flag.engine.models.Item;
import com.flag.engine.models.Like;
import com.flag.engine.models.PMF;
import com.flag.engine.models.UserShopPair;

public class PairUserShop extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int LIKE_SHOP_POINT = 2;
	private static final int LIKE_ITEM_POINT = 1;

	private static final Logger log = Logger.getLogger(PairUserShop.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		long startTime = new Date().getTime();
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		try {
			Query query = pm.newQuery(Like.class);
			List<Like> likes = (List<Like>) pm.newQuery(query).execute();

			log.warning("like da time: " + (new Date().getTime() - startTime) + "ms");

			query = pm.newQuery(Item.class);
			List<Item> items = (List<Item>) pm.newQuery(query).execute();

			log.warning("item da time: " + (new Date().getTime() - startTime) + "ms");

			Map<Long, Long> isMap = new HashMap<Long, Long>();
			for (Item item : items)
				isMap.put(item.getId(), item.getShopId());

			Map<Long, Map<Long, Integer>> uspMap = new HashMap<Long, Map<Long, Integer>>();
			for (Like like : likes) {
				long shopId = 0;
				int p = 0;
				if (like.getType() == Like.TYPE_SHOP) {
					shopId = like.getTargetId();
					p = LIKE_SHOP_POINT;
				} else if (like.getType() == Like.TYPE_ITEM) {
					Long sid = isMap.get(like.getTargetId());
					shopId = (sid != null) ? sid : 0;
					p = LIKE_ITEM_POINT;
				}

				if (shopId != 0 && p != 0) {
					if (uspMap.get(like.getUserId()) == null)
						uspMap.put(like.getUserId(), new HashMap<Long, Integer>());

					Map<Long, Integer> spMap = uspMap.get(like.getUserId());

					Integer point = spMap.get(shopId);
					if (point == null)
						point = 0;
					point += p;

					spMap.put(shopId, point);
				}
			}

			log.warning("usp map process time: " + (new Date().getTime() - startTime) + "ms");

			List<UserShopPair> pairs = new ArrayList<UserShopPair>();
			for (Long userId : uspMap.keySet()) {
				Map<Long, Integer> spMap = uspMap.get(userId);
				for (Long shopId : spMap.keySet()) {
					Integer point = spMap.get(shopId);
					pairs.add(new UserShopPair(userId, shopId, point));
				}
			}

			log.warning("pair time: " + (new Date().getTime() - startTime) + "ms");
			
			query = pm.newQuery(UserShopPair.class);
			List<UserShopPair> oldPairs = (List<UserShopPair>) pm.newQuery(query).execute();

			pm.makePersistentAll(pairs);
			pm.deletePersistentAll(oldPairs);

			log.warning("pair dw time: " + (new Date().getTime() - startTime) + "ms");
			
		} catch (Exception e) {
			log.warning("error: " + (new Date().getTime() - startTime) + "ms");
			e.printStackTrace();
		} finally {
			pm.close();
		}
	}
}
