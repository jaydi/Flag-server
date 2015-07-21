package com.flag.engine.models;

public class UserShopSet extends BaseModel {
	private User user;
	private Shop shop;

	public UserShopSet() {
		super();
	}

	public UserShopSet(User user, Shop shop) {
		super();
		this.user = user;
		this.shop = shop;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

}
