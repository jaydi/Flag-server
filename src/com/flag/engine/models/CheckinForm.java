package com.flag.engine.models;

public class CheckinForm extends BaseModel {
	private long userId;
	private long flagId;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getFlagId() {
		return flagId;
	}

	public void setFlagId(long flagId) {
		this.flagId = flagId;
	}

}
