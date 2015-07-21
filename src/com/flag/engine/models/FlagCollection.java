package com.flag.engine.models;

import java.util.List;

public class FlagCollection extends BaseModel {
	private List<Flag> flags;
	private List<Long> deletedIds;

	public FlagCollection() {
		super();
	}

	public FlagCollection(List<Flag> flags) {
		super();
		this.flags = flags;
	}

	public FlagCollection(List<Flag> flags, List<Long> deletedIds) {
		super();
		this.flags = flags;
		this.deletedIds = deletedIds;
	}

	public List<Flag> getFlags() {
		return flags;
	}

	public void setFlags(List<Flag> flags) {
		this.flags = flags;
	}

	public List<Long> getDeletedIds() {
		return deletedIds;
	}

	public void setDeletedIds(List<Long> deletedIds) {
		this.deletedIds = deletedIds;
	}
}
