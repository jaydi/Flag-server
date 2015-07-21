package com.flag.engine.models;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.flag.engine.utils.CalUtils;

@PersistenceCapable(identityType = IdentityType.APPLICATION, table = "items")
public class Item extends BaseModel {
	public static final int REC_ITEM_SIZE = 20;

	public static final int ITEM_SEX_NONE = 0;
	public static final int ITEM_SEX_FEMALE = 1;
	public static final int ITEM_SEX_MALE = 2;

	public static final int ITEM_TYPE_CLOTHE = 100;
	public static final int ITEM_TYPE_UPPER = 110;
	public static final int ITEM_TYPE_JACKET = 111;
	public static final int ITEM_TYPE_PANTS = 120;
	public static final int ITEM_TYPE_SKIRT = 121;
	public static final int ITEM_TYPE_DRESS = 130;
	public static final int ITEM_TYPE_UNDERWEAR = 140;
	public static final int ITEM_TYPE_SHOES = 200;
	public static final int ITEM_TYPE_BAG = 300;
	public static final int ITEM_TYPE_ACCESSORY = 400;
	public static final int ITEM_TYPE_ELECTRIC = 500;
	public static final int ITEM_TYPE_BEUTY = 600;
	public static final int ITEM_TYPE_HEADWEAR = 700;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
	private Long id;

	@Persistent
	@Index
	private Long shopId;

	@Persistent
	private String name;

	@Persistent
	private String thumbnailUrl;

	@Persistent
	private String description;

	@Persistent
	private int sex;

	@Persistent
	@Index
	private int type;

	@Persistent
	private int sale;

	@Persistent
	private String oldPrice;

	@Persistent
	private String price;

	@Persistent
	@Index
	private String barcodeId;

	@Persistent
	private int reward;

	@Persistent
	private boolean available;

	@NotPersistent
	private boolean rewarded;

	@NotPersistent
	private int likes;

	@NotPersistent
	private boolean liked;

	public Item() {
		super();
	}

	public Item(String[] dataArray) {
		barcodeId = dataArray[0];
		name = dataArray[1];
		description = dataArray[2];
		if (dataArray[4].isEmpty()) {
			price = CalUtils.currencyFormat(dataArray[3]);
			oldPrice = CalUtils.currencyFormat(dataArray[4]);
		} else {
			price = CalUtils.currencyFormat(dataArray[4]);
			oldPrice = CalUtils.currencyFormat(dataArray[3]);
			sale = CalUtils.discountRate(oldPrice, price);
		}
		reward = (int) CalUtils.toNumber(dataArray[5]);
		thumbnailUrl = "";
		available = false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getShopId() {
		return shopId;
	}

	public void setShopId(Long shopId) {
		this.shopId = shopId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSale() {
		return sale;
	}

	public void setSale(int sale) {
		this.sale = sale;
	}

	public String getOldPrice() {
		return oldPrice;
	}

	public void setOldPrice(String oldPrice) {
		this.oldPrice = oldPrice;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getBarcodeId() {
		return barcodeId;
	}

	public void setBarcodeId(String barcodeId) {
		this.barcodeId = barcodeId;
	}

	public int getReward() {
		return reward;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	public boolean isRewarded() {
		return rewarded;
	}

	public void setRewarded(boolean rewarded) {
		this.rewarded = rewarded;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public boolean isLiked() {
		return liked;
	}

	public void setLiked(boolean liked) {
		this.liked = liked;
	}

	public void calSale() {
		if (oldPrice != null && !oldPrice.isEmpty())
			sale = CalUtils.discountRate(oldPrice, price);
	}

	public void update(Item item) {
		if (item.getName() != null && !item.getName().isEmpty())
			name = item.getName();
		if (item.getThumbnailUrl() != null && !item.getThumbnailUrl().isEmpty())
			thumbnailUrl = item.getThumbnailUrl();
		if (item.getDescription() != null && !item.getDescription().isEmpty())
			description = item.getDescription();
		sex = item.getSex();
		type = item.getType();
		if (item.getBarcodeId() != null && !item.getBarcodeId().isEmpty())
			barcodeId = item.getBarcodeId();
		if (item.getPrice() != null && !item.getPrice().isEmpty())
			price = item.getPrice();
		reward = item.getReward();
		if (item.getOldPrice() == null)
			oldPrice = "";
		else
			oldPrice = item.getOldPrice();

		if (oldPrice != null && !oldPrice.isEmpty())
			sale = CalUtils.discountRate(oldPrice, price);
		else
			sale = 0;
	}

	@Override
	public boolean equals(Object o) {
		try {
			Item target = (Item) o;
			return target.getId() == id
					|| (target.getBarcodeId() != null && !target.getBarcodeId().isEmpty() && barcodeId != null && !barcodeId.isEmpty() && target
							.getBarcodeId().equals(barcodeId));
		} catch (ClassCastException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return name + " " + barcodeId;
	}

	public static void setRelatedVariables(List<Item> items, long userId) {
		setLikeVariables(items, userId);
		setRewardVariables(items, userId);
	}

	@SuppressWarnings("unchecked")
	public static void setLikeVariables(List<Item> items, long userId) {
		if (items == null || items.isEmpty())
			return;

		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Like.class);
		query.setFilter("type == itemType");
		query.declareParameters("int itemType");
		List<Like> likes = (List<Like>) pm.newQuery(query).execute(Like.TYPE_ITEM);

		for (Like like : likes)
			for (Item item : items)
				if (like.getTargetId().equals(item.getId())) {
					item.setLikes(item.getLikes() + 1);
					if (like.getUserId().equals(userId))
						item.setLiked(true);
					break;
				}
	}

	@SuppressWarnings("unchecked")
	public static void setRewardVariables(List<Item> items, long userId) {
		if (items == null || items.isEmpty())
			return;

		PersistenceManager pm = PMF.getPersistenceManagerSQL();

		Query query = pm.newQuery(Reward.class);
		query.setFilter("type == itemType && createdAt > daysAgo && userId == theUserId");
		query.declareParameters("int itemType, long daysAgo, long theUserId");
		List<Reward> rewards = (List<Reward>) pm.newQuery(query).execute(Reward.TYPE_ITEM, new Date().getTime() - Reward.EXPIRATION_TIME, userId);

		for (Reward reward : rewards)
			for (Item item : items)
				if (reward.getTargetId().equals(item.getId())) {
					item.setRewarded(true);
					break;
				}
	}
}