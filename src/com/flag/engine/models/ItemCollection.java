package com.flag.engine.models;

import java.util.List;

public class ItemCollection extends BaseModel {
	private List<Item> items;
	private List<Item> hiddenItems;

	public ItemCollection() {
		super();
	}

	public ItemCollection(List<Item> items) {
		super();
		this.items = items;
	}

	public ItemCollection(List<Item> items, List<Item> hiddenItems) {
		super();
		this.items = items;
		this.hiddenItems = hiddenItems;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public List<Item> getHiddenItems() {
		return hiddenItems;
	}

	public void setHiddenItems(List<Item> hiddenItems) {
		this.hiddenItems = hiddenItems;
	}
}
