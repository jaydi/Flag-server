package com.flag.engine.apis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.flag.engine.comparators.ShopPointComparator;
import com.flag.engine.comparators.ShopRewardComparator;
import com.flag.engine.constants.Constants;
import com.flag.engine.models.Flag;
import com.flag.engine.models.PMF;
import com.flag.engine.models.Shop;
import com.flag.engine.models.ShopCollection;
import com.flag.engine.models.UserShopPair;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Shops {
	private static final Logger log = Logger.getLogger(Flags.class.getName());

	@ApiMethod(name = "shops.insert", path = "shop", httpMethod = "post")
	public Shop insert(Shop shop) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		pm.makePersistent(shop);
		pm.close();

		return shop;
	}

	@ApiMethod(name = "shops.get", path = "shop", httpMethod = "get")
	public Shop get(@Nullable @Named("userId") long userId, @Nullable @Named("id") long id) {
		log.warning("userId: " + userId + ", shopId: " + id);

		long startTime = new Date().getTime();
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		Shop shop = null;

		try {
			Shop rawShop = pm.getObjectById(Shop.class, id);

			log.warning("shop da time: " + (new Date().getTime() - startTime) + "ms");

			List<Shop> shops = new ArrayList<Shop>();
			shops.add(rawShop);
			Shop.setRelatedVariables(shops, userId);

			log.warning("rel-var da time: " + (new Date().getTime() - startTime) + "ms");

			shop = shops.get(0);
		} catch (JDOObjectNotFoundException e) {
			return null;
		}

		return shop;
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "shops.update", path = "shop", httpMethod = "put")
	public Shop update(Shop shop) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		Shop target = null;
		List<Shop> brShops = null;

		try {
			target = pm.getObjectById(Shop.class, shop.getId());
			target.update(shop);

			if (target.getType() == Shop.TYPE_HQ) {
				log.warning("update branches");

				Query query = pm.newQuery(Shop.class);
				query.setFilter("parentId == id");
				query.declareParameters("long id");
				brShops = (List<Shop>) pm.newQuery(query).execute(target.getId());

				log.warning("updating branches " + brShops.size());

				for (Shop brShop : brShops)
					brShop.update(shop);
			}

		} catch (JDOObjectNotFoundException e) {
			return null;
		} finally {
			pm.close();
		}

		return target;
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "shops.delete", path = "shop", httpMethod = "delete")
	public void delete(@Named("shopId") Long shopId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		try {
			Shop target = pm.getObjectById(Shop.class, shopId);

			if (target.getType() == Shop.TYPE_HQ) {
				Query query = pm.newQuery(Shop.class);
				query.setFilter("parentId == id");
				query.declareParameters("long id");
				List<Shop> brShops = (List<Shop>) pm.newQuery(query).execute(target.getId());

				pm.deletePersistentAll(brShops);
			}

			pm.deletePersistent(target);

		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "shops.start", path = "shop_start", httpMethod = "get")
	public ShopCollection startShops(@Nullable @Named("userId") long userId, @Nullable @Named("mark") int mark) {
		long startTime = new Date().getTime();
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Shop.class);
		query.setFilter("type == hq");
		query.declareParameters("int hq");
		List<Shop> allShops = (List<Shop>) pm.newQuery(query).execute(Shop.TYPE_HQ);

		if (mark * 10 >= allShops.size())
			return null;

		log.warning("shop da time: " + (new Date().getTime() - startTime) + "ms");

		query = pm.newQuery(UserShopPair.class);
		query.setFilter("userId == id");
		query.declareParameters("long id");
		List<UserShopPair> pairs = (List<UserShopPair>) pm.newQuery(query).execute(userId);

		log.warning("pair da time: " + (new Date().getTime() - startTime) + "ms");

		final Map<Long, Integer> pointMap = new HashMap<Long, Integer>();
		for (UserShopPair pair : pairs)
			pointMap.put(pair.getShopId(), pair.getPoint());

		List<Shop> sortedShops = new ArrayList<Shop>();
		sortedShops.addAll(allShops);
		Collections.sort(sortedShops, new ShopRewardComparator());
		Collections.sort(sortedShops, new ShopPointComparator(pointMap));

		log.warning("point sort process time: " + (new Date().getTime() - startTime) + "ms");

		List<Shop> shops = new ArrayList<Shop>();
		for (int i = mark * 10; i < Math.min(sortedShops.size(), (mark + 1) * 10); i++)
			shops.add(sortedShops.get(i));

		Shop.setRelatedVariables(shops, userId);

		log.warning("shop rel-var da time: " + (new Date().getTime() - startTime) + "ms");

		return new ShopCollection(shops, pointMap);
	}

	@SuppressWarnings("unchecked")
	private List<Shop> listByIds(List<Object> ids) {
		log.warning("list shops: " + ids);
		if (ids == null || ids.isEmpty())
			return new ArrayList<Shop>();

		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		List<Shop> shops = null;

		try {
			shops = (List<Shop>) pm.getObjectsById(ids);
		} catch (JDOObjectNotFoundException e) {
			ids.remove(e.getFailedObject());
			return listByIds(ids);
		}

		return shops;
	}

	@ApiMethod(name = "shops.list", path = "shop_list", httpMethod = "get")
	public ShopCollection list(@Nullable @Named("userId") long userId, @Nullable @Named("ids") List<Long> ids) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		List<Object> shopIds = new ArrayList<Object>();
		for (Long id : ids)
			shopIds.add(pm.newObjectIdInstance(Shop.class, id));

		List<Shop> shops = listByIds(shopIds);

		Shop.setRelatedVariables(shops, userId);

		return new ShopCollection(shops);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "shops.list.reward", path = "shop_list_reward", httpMethod = "get")
	public ShopCollection listReward(@Nullable @Named("userId") long userId, @Nullable @Named("mark") int mark) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Shop.class);
		query.setFilter("reward > zero");
		query.declareParameters("int zero");
		List<Shop> shops = (List<Shop>) pm.newQuery(query).execute(0);

		query = pm.newQuery(Flag.class);
		query.setFilter("reward > zero");
		query.declareParameters("int zero");
		List<Flag> flags = (List<Flag>) pm.newQuery(query).execute(0);

		Shop.setRelatedVariables(shops, userId);

		return new ShopCollection(shops, flags);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "shops.list.provider", path = "shop_list_provider", httpMethod = "get")
	public ShopCollection listProvider(@Nullable @Named("providerId") long providerId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		List<Shop> managingShops = new ArrayList<Shop>();
		List<Shop> branchShops = new ArrayList<Shop>();
		List<Shop> shops = new ArrayList<Shop>();

		Query query = pm.newQuery(Shop.class);
		List<Shop> allShops = (List<Shop>) pm.newQuery(query).execute();

		for (Shop shop : allShops)
			if (shop.getProviderId().equals(providerId))
				managingShops.add(shop);

		for (Shop managingShop : managingShops)
			if (managingShop.getType() == Shop.TYPE_HQ)
				for (Shop shop : allShops)
					if (shop.getType() == Shop.TYPE_BR && shop.getParentId().equals(managingShop.getId()) && !managingShops.contains(shop))
						branchShops.add(shop);

		shops.addAll(managingShops);
		shops.addAll(branchShops);

		return new ShopCollection(shops);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "shops.recommend.near", path = "shop_recommend_near", httpMethod = "get")
	public Shop recommendNear(@Nullable @Named("userId") long userId, @Nullable @Named("ids") List<Long> ids) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		List<Object> keyIds = new ArrayList<Object>();
		for (Long id : ids)
			keyIds.add(pm.newObjectIdInstance(Shop.class, id));
		List<Shop> candidates = listByIds(keyIds);

		if (candidates.isEmpty())
			return null;

		Query query = pm.newQuery(UserShopPair.class);
		query.setFilter("userId == id");
		query.declareParameters("long id");
		List<UserShopPair> pairs = (List<UserShopPair>) pm.newQuery(query).execute(userId);

		Map<Long, Integer> pointMap = new HashMap<Long, Integer>();
		for (UserShopPair pair : pairs)
			pointMap.put(pair.getShopId(), pair.getPoint());

		Collections.sort(candidates, new ShopPointComparator(pointMap));

		Shop.setRewardVariables(candidates, userId);

		Collections.sort(candidates, new ShopRewardComparator());

		Shop shop = candidates.get(0);
		if (pointMap.get((shop.getType() == Shop.TYPE_BR) ? shop.getParentId() : shop.getId()) != null
				|| (shop.getReward() > 0 && !shop.isRewarded()))
			return shop;
		else
			return null;
	}

}