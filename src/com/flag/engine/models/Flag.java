package com.flag.engine.models;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, table = "flags")
public class Flag extends BaseModel {
	@Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
	@PrimaryKey
	private Long id;

	@Persistent
	@Index
	private double lat;

	@Persistent
	@Index
	private double lon;

	@Persistent
	private long createdAt;

	@Persistent
	@Index
	private Long shopId;

	@Persistent
	private String shopName;

	@Persistent
	private int shopType;

	@Persistent
	private int reward;

	@NotPersistent
	private double distance;
	
	public Flag() {
		super();
	}

	public Flag(Flag flag) {
		super();
		this.id = flag.getId();
		this.lat = flag.getLat();
		this.lon = flag.getLon();
		this.createdAt = flag.getCreatedAt();
		this.shopId = flag.getShopId();
		this.shopName = flag.getShopName();
		this.shopType = flag.getShopType();
		this.reward = flag.getReward();
		this.distance = flag.getDistance();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public Long getShopId() {
		return shopId;
	}

	public void setShopId(Long shopId) {
		this.shopId = shopId;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public int getShopType() {
		return shopType;
	}

	public void setShopType(int shopType) {
		this.shopType = shopType;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=" + id).append(", lat=" + lat).append(", lon=" + lon).append(", createdAt=" + createdAt).append(", shopId=" + shopId)
				.append(", shopName=" + shopName);
		return sb.toString();
	}
}
