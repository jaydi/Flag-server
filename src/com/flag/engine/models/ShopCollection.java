package com.flag.engine.models;

import java.util.List;
import java.util.Map;

public class ShopCollection extends BaseModel {
	private List<Shop> shops;
	private List<Flag> flags;
	private Map<Long, Integer> pointMap;

	public ShopCollection() {
		super();
	}

	public ShopCollection(List<Shop> shops) {
		super();
		this.shops = shops;
	}

	public ShopCollection(List<Shop> shops, List<Flag> flags) {
		super();
		this.shops = shops;
		this.flags = flags;
	}

	public ShopCollection(List<Shop> shops, Map<Long, Integer> pointMap) {
		super();
		this.shops = shops;
		this.pointMap = pointMap;
	}

	public List<Shop> getShops() {
		return shops;
	}

	public void setShops(List<Shop> shops) {
		this.shops = shops;
	}

	public List<Flag> getFlags() {
		return flags;
	}

	public void setFlags(List<Flag> flags) {
		this.flags = flags;
	}

	public Map<Long, Integer> getPointMap() {
		return pointMap;
	}

	public void setPointMap(Map<Long, Integer> pointMap) {
		this.pointMap = pointMap;
	}
	
}
