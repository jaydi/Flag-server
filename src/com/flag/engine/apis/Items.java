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

import com.flag.engine.comparators.ItemIdRelationComparator;
import com.flag.engine.constants.Constants;
import com.flag.engine.models.BranchItemMatcher;
import com.flag.engine.models.Item;
import com.flag.engine.models.ItemCollection;
import com.flag.engine.models.ItemId;
import com.flag.engine.models.ItemViewPair;
import com.flag.engine.models.Like;
import com.flag.engine.models.PMF;
import com.flag.engine.models.Shop;
import com.flag.engine.models.UserInfo;
import com.flag.engine.utils.CalUtils;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "flagengine", version = "v1", clientIds = { Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID,
		Constants.API_EXPLORER_CLIENT_ID }, audiences = { Constants.ANDROID_AUDIENCE })
public class Items {
	private static final Logger log = Logger.getLogger(Items.class.getName());

	@ApiMethod(name = "items.insert", path = "item", httpMethod = "post")
	public Item insert(Item item) {
		log.info("insert item: " + item.toString());

		item.calSale();

		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		pm.makePersistent(item);
		pm.close();

		return item;
	}

	@ApiMethod(name = "items.get", path = "one_item", httpMethod = "get")
	public Item get(@Nullable @Named("userId") long userId, @Nullable @Named("itemId") long itemId) {
		log.info("get item: " + itemId);

		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		List<Item> items = new ArrayList<Item>();

		try {
			items.add(pm.getObjectById(Item.class, itemId));
			Item.setRelatedVariables(items, userId);

		} catch (JDOObjectNotFoundException e) {
		}

		return items.get(0);
	}

	@ApiMethod(name = "items.update", path = "item", httpMethod = "put")
	public Item update(Item item) {
		log.info("update item: " + item.toString());

		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		Item target = null;

		try {
			target = pm.getObjectById(Item.class, item.getId());
			target.update(item);

		} catch (JDOObjectNotFoundException e) {
			return null;
		} finally {
			pm.close();
		}

		return target;
	}

	@ApiMethod(name = "items.delete", path = "item", httpMethod = "delete")
	public void delete(@Nullable @Named("itemId") long itemId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		try {
			Item item = pm.getObjectById(Item.class, itemId);
			pm.deletePersistent(item);

		} catch (JDOObjectNotFoundException e) {
		} finally {
			pm.close();
		}
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "items.init", path = "item_init", httpMethod = "get")
	public ItemCollection initItems(@Nullable @Named("userId") long userId, @Nullable @Named("mark") int mark) {
		long startTime = new Date().getTime();
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		int sex = 0;
		try {
			UserInfo userInfo = pm.getObjectById(UserInfo.class, userId);
			sex = userInfo.getSex();
		} catch (JDOObjectNotFoundException e) {
		}

		List<ItemId> itemIds;
		Query query = pm.newQuery(ItemId.class);
		query.setRange(mark * 20, (mark + 1) * 20);
		if (sex != 0) {
			query.setFilter("sex == unisex || sex == gender");
			query.declareParameters("int unisex, int gender");
			itemIds = (List<ItemId>) pm.newQuery(query).execute(Item.ITEM_SEX_NONE, sex);
		} else
			itemIds = (List<ItemId>) pm.newQuery(query).execute();

		log.warning("item id da time: " + (new Date().getTime() - startTime) + "ms");

		List<Object> ids = new ArrayList<Object>();
		for (ItemId itemId : itemIds)
			ids.add(pm.newObjectIdInstance(Item.class, itemId.getItemId()));
		List<Item> items = listByIds(ids);

		log.warning("item da time: " + (new Date().getTime() - startTime) + "ms");

		Item.setRelatedVariables(items, userId);

		log.warning("rel-var da time: " + (new Date().getTime() - startTime) + "ms");

		return new ItemCollection(items);
	}

	@SuppressWarnings("unchecked")
	private List<Item> listByIds(List<Object> ids) {
		log.warning("list items: " + ids);
		if (ids == null || ids.isEmpty())
			return new ArrayList<Item>();

		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		List<Item> items = new ArrayList<Item>();

		try {
			List<Item> canItems = (List<Item>) pm.getObjectsById(ids);
			for (Item canItem : canItems)
				if (canItem.isAvailable())
					items.add(canItem);
		} catch (JDOObjectNotFoundException e) {
			ids.remove(e.getFailedObject());
			return listByIds(ids);
		}

		return items;
	}

	@ApiMethod(name = "items.list.ids", path = "item_list", httpMethod = "get")
	public ItemCollection list(@Nullable @Named("userId") long userId, @Nullable @Named("ids") List<Long> ids) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		List<Object> keyIds = new ArrayList<Object>();
		for (Long id : ids)
			keyIds.add(pm.newObjectIdInstance(Item.class, id));

		List<Item> items = listByIds(keyIds);

		Item.setRelatedVariables(items, userId);

		return new ItemCollection(items);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "items.list", path = "item", httpMethod = "get")
	public ItemCollection listByShop(@Nullable @Named("userId") long userId, @Nullable @Named("shopId") long shopId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		Shop shop = null;
		List<Item> items = null;

		try {
			shop = pm.getObjectById(Shop.class, shopId);
		} catch (JDOObjectNotFoundException e) {
			return null;
		}

		if (shop.getType() == Shop.TYPE_BR) {
			Query query = pm.newQuery(BranchItemMatcher.class);
			query.setFilter("branchShopId == shopId");
			query.declareParameters("long shopId");
			List<BranchItemMatcher> matchers = (List<BranchItemMatcher>) pm.newQuery(query).execute(shopId);

			List<Object> ids = new ArrayList<Object>();
			for (BranchItemMatcher matcher : matchers)
				ids.add(pm.newObjectIdInstance(Item.class, matcher.getItemId()));

			items = listByIds(ids);
		} else {
			Query query = pm.newQuery(Item.class);
			query.setFilter("available == yes && shopId == theShopId");
			query.declareParameters("boolean yes, long theShopId");
			items = (List<Item>) pm.newQuery(query).execute(true, shopId);
		}

		Item.setRelatedVariables(items, userId);

		return new ItemCollection(items);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "items.list.user", path = "item_user", httpMethod = "get")
	public ItemCollection listByUser(@Nullable @Named("userId") long userId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Like.class);
		query.setFilter("userId == id && type == itemType");
		query.declareParameters("long id, int itemType");
		List<Like> likes = (List<Like>) pm.newQuery(query).execute(userId, Like.TYPE_ITEM);

		List<Object> ids = new ArrayList<Object>();
		for (Like like : likes)
			ids.add(pm.newObjectIdInstance(Item.class, like.getTargetId()));

		List<Item> items = listByIds(ids);

		Item.setRelatedVariables(items, userId);

		return new ItemCollection(items);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "items.list.item", path = "item_item", httpMethod = "get")
	public ItemCollection listItem(@Nullable @Named("userId") long userId, @Nullable @Named("itemId") long itemId) {
		long startTime = new Date().getTime();
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		List<Item> items = new ArrayList<Item>();

		Query query = pm.newQuery(ItemViewPair.class);
		query.setFilter("preId == itemId");
		query.declareParameters("long itemId");
		List<ItemViewPair> pairs = (List<ItemViewPair>) pm.newQuery(query).execute(itemId);

		List<Long> ids = new ArrayList<Long>();
		Map<Long, Integer> countMap = new HashMap<Long, Integer>();

		for (ItemViewPair pair : pairs) {
			if (!ids.contains(pair.getNextId()))
				ids.add(pair.getNextId());

			if (countMap.get(pair.getNextId()) == null)
				countMap.put(pair.getNextId(), 1);
			else
				countMap.put(pair.getNextId(), countMap.get(pair.getNextId()) + 1);
		}

		Collections.sort(ids, new ItemIdRelationComparator(countMap));

		List<Object> keyIds = new ArrayList<Object>();
		for (int i = 0; i < Math.min(10, ids.size()); i++)
			keyIds.add(pm.newObjectIdInstance(Item.class, ids.get(i)));
		items.addAll(listByIds(keyIds));

		if (items.size() < Item.REC_ITEM_SIZE) {
			int pickSize = Item.REC_ITEM_SIZE - items.size();

			Item item = null;
			try {
				item = pm.getObjectById(Item.class, itemId);
			} catch (JDOObjectNotFoundException e) {
				return new ItemCollection(items);
			}

			log.warning("item da time: " + (new Date().getTime() - startTime) + "ms");

			query = pm.newQuery(Item.class);
			query.setFilter("available == yes && (sex == theSex || sex == none) && type == theType && id != itemId");
			query.declareParameters("boolean yes, int theSex, int none, int theType, long itemId");
			List<Item> simItems = (List<Item>) pm.newQuery(query).executeWithArray(true, item.getSex(), Item.ITEM_SEX_NONE, item.getType(),
					item.getId());

			log.warning("type-rec item da time: " + (new Date().getTime() - startTime) + "ms");

			List<Item> canItems = new ArrayList<Item>();
			for (Item simItem : simItems)
				if (!items.contains(simItem))
					canItems.add(simItem);

			if (canItems.size() <= pickSize)
				items.addAll(canItems);
			else {
				List<Integer> picks = CalUtils.randomIndexes(canItems.size(), pickSize);
				for (Integer pick : picks)
					items.add(canItems.get(pick));
			}

			log.warning("type-rec item add time: " + (new Date().getTime() - startTime) + "ms");
		}

		Item.setRelatedVariables(items, userId);

		log.warning("item rel-var da time: " + (new Date().getTime() - startTime) + "ms");

		return new ItemCollection(items);
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "items.list.provider", path = "item_provider", httpMethod = "get")
	public ItemCollection listByProvider(@Nullable @Named("shopId") long shopId) {
		log.info("list item for manager: " + shopId);

		PersistenceManager pm = PMF.getPersistenceManagerSQL();
		Shop shop = null;
		List<Item> items = new ArrayList<Item>();
		List<Item> hiddenItems = new ArrayList<Item>();

		try {
			shop = pm.getObjectById(Shop.class, shopId);
		} catch (JDOObjectNotFoundException e) {
			return null;
		}

		if (shop.getType() == Shop.TYPE_BR) {
			items.addAll(listByShop(0, shop.getId()).getItems());

			if (shop.getParentId() != 0) {
				Query query = pm.newQuery(Item.class);
				query.setFilter("shopId == parentId");
				query.declareParameters("long parentId");
				List<Item> hqItems = (List<Item>) pm.newQuery(query).execute(shop.getParentId());

				for (Item hqItem : hqItems)
					if (!items.contains(hqItem))
						hiddenItems.add(hqItem);
			}
		} else {
			Query query = pm.newQuery(Item.class);
			query.setFilter("shopId == id");
			query.declareParameters("long id");
			List<Item> hqItems = (List<Item>) pm.newQuery(query).execute(shop.getId());

			for (Item hqItem : hqItems)
				if (hqItem.isAvailable())
					items.add(hqItem);
				else
					hiddenItems.add(hqItem);
		}

		Item.setLikeVariables(items, 0);

		return new ItemCollection(items, hiddenItems);
	}

	@ApiMethod(name = "items.branch.expose", path = "item_branch", httpMethod = "post")
	public BranchItemMatcher expose(BranchItemMatcher matcher) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		pm.makePersistent(matcher);

		try {
			Item item = pm.getObjectById(Item.class, matcher.getItemId());
			if (!item.isAvailable())
				item.setAvailable(true);
		} catch (JDOObjectNotFoundException e) {
			return null;
		} finally {
			pm.close();
		}

		return matcher;
	}

	@SuppressWarnings("unchecked")
	@ApiMethod(name = "items.branch.hide", path = "item_branch", httpMethod = "delete")
	public void hide(@Nullable @Named("shopId") long shopId, @Nullable @Named("itemId") long itemId) {
		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(BranchItemMatcher.class);
		query.setFilter("branchShopId == shopId && itemId == theItemId");
		query.declareParameters("long shopId, long theItemId");
		List<BranchItemMatcher> matchers = (List<BranchItemMatcher>) pm.newQuery(query).execute(shopId, itemId);

		pm.deletePersistentAll(matchers);

		query.setFilter("itemId == theItemId");
		query.declareParameters("long theItemId");
		matchers = (List<BranchItemMatcher>) pm.newQuery(query).execute(itemId);

		if (matchers.isEmpty())
			try {
				Item item = pm.getObjectById(Item.class, itemId);
				item.setAvailable(false);
			} catch (JDOObjectNotFoundException e) {
			}

		pm.close();
	}

	// @SuppressWarnings({ "unchecked" })
	// @ApiMethod(name = "items.list.reward", path = "item_reward", httpMethod = "get")
	// public ItemCollection listReward(@Nullable @Named("userId") long userId, @Nullable @Named("mark") int mark) {
	// PersistenceManager pm = PMF.getPersistenceManagerSQL();
	//
	// Query query = pm.newQuery(BranchItemMatcher.class);
	// query.setFilter("rewardable == True");
	// query.declareParameters("boolean True");
	// query.setOrdering("id desc");
	// List<BranchItemMatcher> matchers = (List<BranchItemMatcher>) pm.newQuery(query).execute(true);
	//
	// List<Long> itemIds = new ArrayList<Long>();
	// for (BranchItemMatcher matcher : matchers)
	// if (!itemIds.contains(matcher.getItemId()))
	// itemIds.add(matcher.getItemId());
	//
	// List<Object> ids = new ArrayList<Object>();
	// for (int i = mark * 20; i < (mark + 1) * 20; i++)
	// try {
	// ids.add(pm.newObjectIdInstance(Item.class, itemIds.get(i)));
	// } catch (IndexOutOfBoundsException e) {
	// break;
	// }
	//
	// if (ids.isEmpty())
	// return null;
	//
	// List<Item> items = listByIds(ids);
	// Item.setRelatedVariables(items, userId);
	//
	// List<Item> ableItems = new ArrayList<Item>();
	// for (Item item : items)
	// if (!item.isRewarded())
	// ableItems.add(item);
	//
	// return new ItemCollection(items);
	// }

	// @SuppressWarnings("unchecked")
	// @ApiMethod(name = "items.branch.reward", path = "item_branch", httpMethod = "put")
	// public BranchItemMatcher toggleReward(BranchItemMatcher matcher) {
	// PersistenceManager pm = PMF.getPersistenceManagerSQL();
	//
	// Query query = pm.newQuery(BranchItemMatcher.class);
	// query.setFilter("branchShopId == shopId && itemId == theItemId");
	// query.declareParameters("long shopId, long theItemId");
	// List<BranchItemMatcher> matchers = (List<BranchItemMatcher>) pm.newQuery(query).execute(matcher.getBranchShopId(), matcher.getItemId());
	//
	// BranchItemMatcher target = null;
	// if (!matchers.isEmpty()) {
	// target = matchers.get(0);
	// if (target.isRewardable())
	// target.setRewardable(false);
	// else
	// target.setRewardable(true);
	// }
	//
	// pm.close();
	//
	// return target;
	// }
}
