package com.flag.engine.apis;

import java.util.ArrayList;
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

import com.flag.engine.constants.Constants;
import com.flag.engine.models.BranchItemMatcher;
import com.flag.engine.models.Flag;
import com.flag.engine.models.FlagCollection;
import com.flag.engine.models.FlagDeletionTag;
import com.flag.engine.models.PMF;
import com.flag.engine.models.Shop;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Flags {
	private static final Logger log = Logger.getLogger(Flags.class.getName());

	@ApiMethod(name = "flags.insert", path = "flag", httpMethod = "post")
	public Flag insert(Flag flag) {
		log.info("insert flag: " + flag.toString());

		flag.setCreatedAt(new Date().getTime());

		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		pm.makePersistent(flag);
		pm.close();

		return flag;
	}

	@ApiMethod(name = "flags.delete", path = "flag", httpMethod = "delete")
	public void delete(@Named("flagId") Long flagId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		try {
			Flag target = pm.getObjectById(Flag.class, flagId);
			pm.deletePersistent(target);

		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "flags.list.all", path = "flag_list_all", httpMethod = "get")
	public FlagCollection listAll(@Nullable @Named("tag") long tag) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Flag.class);
		query.setFilter("createdAt > tag");
		query.declareParameters("long tag");
		List<Flag> flags = (List<Flag>) pm.newQuery(query).execute(tag);

		query = pm.newQuery(FlagDeletionTag.class);
		query.setFilter("createdAt > tag");
		query.declareParameters("long tag");
		List<FlagDeletionTag> deletedTags = (List<FlagDeletionTag>) pm.newQuery(query).execute(tag);

		List<Long> deletedIds = new ArrayList<Long>();
		for (FlagDeletionTag deletedTag : deletedTags)
			deletedIds.add(deletedTag.getFlagId());

		return new FlagCollection(flags, deletedIds);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "flags.list.byshop", path = "flag_list_byshop", httpMethod = "get")
	public FlagCollection listByShop(@Nullable @Named("shopId") long shopId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		Shop shop;

		try {
			shop = pm.getObjectById(Shop.class, shopId);
		} catch (JDOObjectNotFoundException e) {
			return null;
		}

		Query query;
		StringBuilder sbFilter = new StringBuilder("shopId == id");
		StringBuilder sbParam = new StringBuilder("long id");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("id", shopId);

		if (shop.getType() == Shop.TYPE_HQ) {
			query = pm.newQuery(Shop.class);
			query.setFilter("parentId == shopId");
			query.declareParameters("long shopId");
			List<Shop> brShops = (List<Shop>) pm.newQuery(query).execute(shopId);

			for (int i = 0; i < brShops.size(); i++) {
				sbFilter.append(" || shopId == id" + i);
				sbParam.append(", long id" + i);
				paramMap.put("id" + i, brShops.get(i).getId());
			}
		}

		query = pm.newQuery(Flag.class);
		query.setFilter(sbFilter.toString());
		query.declareParameters(sbParam.toString());
		List<Flag> flags = (List<Flag>) pm.newQuery(query).executeWithMap(paramMap);

		return new FlagCollection(flags);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "flags.list.byreward", path = "flag_list_byreward", httpMethod = "get")
	public FlagCollection listReward(@Nullable @Named("lat") double lat, @Nullable @Named("lon") double lon) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Flag.class);
		query.setFilter("reward > zero");
		query.declareParameters("int zero");
		List<Flag> flags = (List<Flag>) pm.newQuery(query).execute(0);

		return new FlagCollection(flags);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "flags.list.byitem", path = "flag_list_byitem", httpMethod = "get")
	public FlagCollection listByItem(@Nullable @Named("itemId") long itemId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(BranchItemMatcher.class);
		query.setFilter("itemId == id");
		query.declareParameters("long id");
		List<BranchItemMatcher> matchers = (List<BranchItemMatcher>) pm.newQuery(query).execute(itemId);

		if (matchers.isEmpty())
			return null;

		StringBuilder sbFilter = new StringBuilder();
		StringBuilder sbParam = new StringBuilder();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		int i = 0;

		for (BranchItemMatcher matcher : matchers) {
			if (sbFilter.length() > 0) {
				sbFilter.append(" || ");
				sbParam.append(", ");
			}

			sbFilter.append("shopId == id" + i);
			sbParam.append("long id" + i);
			paramMap.put("id" + i, matcher.getBranchShopId());
		}

		query = pm.newQuery(Flag.class);
		query.setFilter(sbFilter.toString());
		query.declareParameters(sbParam.toString());
		List<Flag> flags = (List<Flag>) pm.newQuery(query).executeWithMap(paramMap);

		return new FlagCollection(flags);
	}
}
